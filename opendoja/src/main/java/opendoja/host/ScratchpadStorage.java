package opendoja.host;

import com.nttdocomo.io.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

final class ScratchpadStorage {
    private static final int HEADER_BYTES = 64;
    private static final int HEADER_ENTRIES = 16;

    private final Path legacyRoot;
    private final Path packedFile;
    private final int[] configuredSizes;
    private volatile boolean warnedHeaderMismatch;

    ScratchpadStorage(Path legacyRoot, Path packedFile, int[] configuredSizes) {
        this.legacyRoot = legacyRoot;
        this.packedFile = packedFile;
        this.configuredSizes = configuredSizes == null ? new int[0] : configuredSizes.clone();
    }

    void initialize() throws IOException {
        if (packedFile != null) {
            Path parent = packedFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return;
        }
        if (legacyRoot != null) {
            Files.createDirectories(legacyRoot);
        }
    }

    InputStream openInput(int index, long position, long length) throws IOException {
        if (packedFile == null) {
            OpenDoJaLog.debug(ScratchpadStorage.class,
                    "openInput legacy index=" + index + " pos=" + position + " len=" + length);
            return ScratchpadStreams.openInput(legacyFile(index), position, length);
        }
        ScratchpadAccess access = resolvePackedAccess(index, position, length);
        OpenDoJaLog.debug(ScratchpadStorage.class,
                "openInput index=" + index + " pos=" + position + " len=" + length
                        + " -> offset=" + access.offset() + " fileLen=" + access.length());
        return ScratchpadStreams.openInput(packedFile, access.offset(), access.length());
    }

    OutputStream openOutput(int index, long position, long length) throws IOException {
        if (packedFile == null) {
            return ScratchpadStreams.openOutput(legacyFile(index), position, length);
        }
        ScratchpadAccess access = resolvePackedAccess(index, position, length);
        return ScratchpadStreams.openOutput(packedFile, access.offset(), access.length());
    }

    private Path legacyFile(int index) {
        return legacyRoot.resolve("sp-" + index + ".bin");
    }

    private ScratchpadAccess resolvePackedAccess(int index, long position, long requestedLength) throws IOException {
        PackedLayout layout = loadPackedLayout();
        if (layout.packed()) {
            ensurePackedFile(layout);
            return layout.access(index, position, requestedLength);
        }
        long normalizedPosition = Math.max(0L, position);
        if (index != 0) {
            OpenDoJaLog.warn(ScratchpadStorage.class,
                    () -> "Packed .sp backing " + packedFile + " has no segment table; treating scratchpad index "
                            + index + " as index 0");
        }
        return new ScratchpadAccess(normalizedPosition, requestedLength);
    }

    private void ensurePackedFile(PackedLayout layout) throws IOException {
        if (!layout.synthetic()) {
            return;
        }
        Path parent = packedFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (Files.exists(packedFile) && Files.size(packedFile) > 0L) {
            return;
        }
        try (RandomAccessFile file = new RandomAccessFile(packedFile.toFile(), "rw")) {
            file.setLength(layout.totalBytes());
        }
    }

    private PackedLayout loadPackedLayout() throws IOException {
        if (packedFile == null) {
            return PackedLayout.single();
        }
        if (Files.exists(packedFile)) {
            long fileSize = Files.size(packedFile);
            if (configuredSizes.length > 0) {
                return configuredLayoutFor(fileSize);
            }
            if (fileSize > 0L) {
                return PackedLayout.single();
            }
        }
        if (configuredSizes.length > 0) {
            return PackedLayout.configured(configuredSizes, 0L, true);
        }
        return PackedLayout.single();
    }

    private PackedLayout configuredLayoutFor(long fileSize) throws IOException {
        long declaredPayloadBytes = declaredPayloadBytes(configuredSizes);
        // Exact declared size is a raw device-visible scratchpad payload with no host header.
        if (fileSize == declaredPayloadBytes) {
            return PackedLayout.configured(configuredSizes, 0L, false);
        }
        // Headered files must prove that the first 64 bytes are a little-endian dojaemu segment table.
        // Size alone is not enough: an oversized raw or corrupt file must not be shifted by 64 bytes.
        if (hasConfiguredHeader()) {
            long realPayloadBytes = fileSize - HEADER_BYTES;
            if (realPayloadBytes > declaredPayloadBytes) {
                warnConfiguredSizeOverride(fileSize, declaredPayloadBytes, realPayloadBytes, true);
                return PackedLayout.configured(sizesForPayloadBytes(realPayloadBytes), HEADER_BYTES, false);
            }
            if (realPayloadBytes < declaredPayloadBytes) {
                warnConfiguredSizeMismatch(fileSize, declaredPayloadBytes,
                        "; detected a matching 64-byte header and treating the payload as truncated");
            }
            return PackedLayout.configured(configuredSizes, HEADER_BYTES, false);
        }
        if (fileSize > declaredPayloadBytes) {
            warnConfiguredSizeOverride(fileSize, declaredPayloadBytes, fileSize, false);
            return PackedLayout.configured(sizesForPayloadBytes(fileSize), 0L, false);
        }
        // Preserve the historical mismatch fallback while making the questionable layout visible in logs.
        warnConfiguredSizeMismatch(fileSize, declaredPayloadBytes);
        return PackedLayout.configured(configuredSizes, 0L, false);
    }

    private boolean hasConfiguredHeader() throws IOException {
        byte[] header;
        try (InputStream input = Files.newInputStream(packedFile)) {
            header = opendoja.host.ByteStreams.readN(input, HEADER_BYTES);
        }
        if (header.length < HEADER_BYTES) {
            return false;
        }
        return headerMatchesConfiguredSizes(header);
    }

    private boolean headerMatchesConfiguredSizes(byte[] header) {
        for (int i = 0; i < HEADER_ENTRIES; i++) {
            int actual = readLittleEndianInt(header, i * Integer.BYTES);
            if (i < configuredSizes.length) {
                if (actual != Math.max(0, configuredSizes[i])) {
                    return false;
                }
            } else if (actual != -1) {
                return false;
            }
        }
        return true;
    }

    private static int readLittleEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xFF)
                | ((bytes[offset + 1] & 0xFF) << 8)
                | ((bytes[offset + 2] & 0xFF) << 16)
                | ((bytes[offset + 3] & 0xFF) << 24);
    }

    private void warnConfiguredSizeMismatch(long fileSize, long declaredPayloadBytes) {
        warnConfiguredSizeMismatch(fileSize, declaredPayloadBytes, "");
    }

    private void warnConfiguredSizeMismatch(long fileSize, long declaredPayloadBytes, String detail) {
        if (warnedHeaderMismatch) {
            return;
        }
        warnedHeaderMismatch = true;
        OpenDoJaLog.warn(ScratchpadStorage.class,
                () -> "Packed .sp backing " + packedFile
                        + " declares " + declaredPayloadBytes
                        + " bytes but file size is " + fileSize
                        + "; expected either " + declaredPayloadBytes
                        + " (raw) or " + (declaredPayloadBytes + HEADER_BYTES)
                        + " (with 64-byte dojaemu header)" + detail);
    }

    private void warnConfiguredSizeOverride(long fileSize, long declaredPayloadBytes, long realPayloadBytes,
                                            boolean headered) {
        if (warnedHeaderMismatch) {
            return;
        }
        warnedHeaderMismatch = true;
        OpenDoJaLog.warn(ScratchpadStorage.class,
                () -> "Packed .sp backing " + packedFile
                        + " declares " + declaredPayloadBytes
                        + " bytes via SPsize but real .sp payload is " + realPayloadBytes
                        + " bytes" + (headered ? " after the 64-byte dojaemu header" : "")
                        + " (file size " + fileSize + "); overriding declared SPsize in memory so the entire file "
                        + "is accessible");
    }

    private int[] sizesForPayloadBytes(long payloadBytes) throws IOException {
        long declaredPayloadBytes = declaredPayloadBytes(configuredSizes);
        if (payloadBytes <= declaredPayloadBytes) {
            return configuredSizes;
        }
        int[] sizes = configuredSizes.clone();
        int lastIndex = Math.min(sizes.length, HEADER_ENTRIES) - 1;
        long adjustedSize = (long) Math.max(0, sizes[lastIndex]) + payloadBytes - declaredPayloadBytes;
        if (adjustedSize > Integer.MAX_VALUE) {
            throw new IOException("Packed .sp backing " + packedFile
                    + " is too large to map into scratchpad segment " + lastIndex);
        }
        sizes[lastIndex] = (int) adjustedSize;
        return sizes;
    }

    private static long declaredPayloadBytes(int[] sizes) {
        long total = 0L;
        for (int size : sizes) {
            total += Math.max(0, size);
        }
        return total;
    }

    private record ScratchpadAccess(long offset, long length) {
    }

    private static final class PackedLayout {
        private final int[] sizes;
        private final long[] offsets;
        private final boolean packed;
        private final boolean synthetic;

        private PackedLayout(int[] sizes, long[] offsets, boolean packed, boolean synthetic) {
            this.sizes = sizes;
            this.offsets = offsets;
            this.packed = packed;
            this.synthetic = synthetic;
        }

        private static PackedLayout configured(int[] configuredSizes, long dataStart, boolean synthetic) {
            int[] sizes = new int[HEADER_ENTRIES];
            long[] offsets = new long[HEADER_ENTRIES];
            long offset = Math.max(0L, dataStart);
            for (int i = 0; i < HEADER_ENTRIES; i++) {
                int size = i < configuredSizes.length ? Math.max(0, configuredSizes[i]) : 0;
                sizes[i] = size;
                offsets[i] = offset;
                offset += size;
            }
            return new PackedLayout(sizes, offsets, true, synthetic);
        }

        private static PackedLayout single() {
            return new PackedLayout(new int[0], new long[0], false, false);
        }

        boolean packed() {
            return packed;
        }

        boolean synthetic() {
            return synthetic;
        }

        long totalBytes() {
            if (!packed) {
                return 0L;
            }
            return offsets.length == 0 ? 0L : offsets[offsets.length - 1] + sizes[sizes.length - 1];
        }

        ScratchpadAccess access(int index, long position, long requestedLength) throws IOException {
            int safeIndex = index < 0 || index >= HEADER_ENTRIES ? HEADER_ENTRIES : index;
            long normalizedPosition = Math.max(0L, position);
            if (safeIndex >= HEADER_ENTRIES) {
                throw scratchpadOversize("Scratchpad segment " + index + " is out of range");
            }
            int segmentSize = sizes[safeIndex];
            if (normalizedPosition > segmentSize) {
                throw scratchpadOversize("Scratchpad segment " + index
                        + " position " + normalizedPosition
                        + " exceeds size " + segmentSize);
            }
            long available = Math.max(0L, segmentSize - normalizedPosition);
            if (requestedLength >= 0L && requestedLength > available) {
                throw scratchpadOversize("Scratchpad segment " + index
                        + " range [" + normalizedPosition + ", " + (normalizedPosition + requestedLength)
                        + ") exceeds size " + segmentSize);
            }
            long boundedLength = requestedLength < 0L ? available : requestedLength;
            return new ScratchpadAccess(offsets[safeIndex] + normalizedPosition, boundedLength);
        }

        private static ConnectionException scratchpadOversize(String message) {
            return new ConnectionException(ConnectionException.SCRATCHPAD_OVERSIZE, message);
        }
    }
}
