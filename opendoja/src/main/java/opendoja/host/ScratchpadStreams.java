package opendoja.host;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.Objects;

public final class ScratchpadStreams {
    private ScratchpadStreams() {
    }

    public static InputStream openInput(Path path, long offset) throws IOException {
        return openInput(path, offset, -1L);
    }

    public static InputStream openInput(Path path, long offset, long length) throws IOException {
        ensureParentDirectory(path, false);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (FileAlreadyExistsException ignored) {
                // A concurrent opener created the file between the check and the create.
            }
        }
        RandomAccessFile file = new RandomAccessFile(path.toFile(), "r");
        return new ScratchpadInputStream(file, Math.max(0L, offset), length);
    }

    public static OutputStream openOutput(Path path, long offset) throws IOException {
        return openOutput(path, offset, -1L);
    }

    public static OutputStream openOutput(Path path, long offset, long length) throws IOException {
        ensureParentDirectory(path, true);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (FileAlreadyExistsException ignored) {
                // A concurrent opener created the file between the check and the create.
            }
        }
        RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw");
        file.seek(Math.max(0L, offset));
        OutputStream stream = new RandomAccessFileOutputStream(file);
        if (length >= 0L) {
            stream = new BoundedOutputStream(stream, length);
        }
        return new FilterOutputStream(stream) {
            @Override
            public void close() throws IOException {
                super.close();
                file.close();
            }
        };
    }

    private static volatile boolean warnedParentState;

    /**
     * Prepares the parent directory of a scratchpad backing file. Reads never fail
     * here: the subsequent open surfaces the real problem with an accurate error.
     * Parent-creation races (e.g. a spurious FileAlreadyExistsException while the
     * directory demonstrably exists) are logged once with full path state instead
     * of being propagated — propagating them used to hit the game's frame-loop
     * catch block and permanently freeze titles like FF4.
     */
    private static void ensureParentDirectory(Path path, boolean forWrite) throws IOException {
        Path parent = path.getParent();
        if (parent == null || Files.isDirectory(parent)) {
            return;
        }
        if (Files.exists(parent)) {
            logParentStateOnce(path, parent, "exists but is not a directory");
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            logParentStateOnce(path, parent, "createDirectories failed: " + exception);
            if (forWrite && !Files.isDirectory(parent)) {
                throw exception;
            }
        }
    }

    private static void logParentStateOnce(Path path, Path parent, String reason) {
        if (warnedParentState) {
            return;
        }
        warnedParentState = true;
        final String state = inspectParentState(parent);
        final String detail = reason;
        OpenDoJaLog.warn(ScratchpadStreams.class,
                () -> "scratchpad parent " + parent + " for " + path + ": " + detail + "; " + state);
    }

    private static String inspectParentState(Path parent) {
        try {
            return "exists=" + Files.exists(parent)
                    + " isDirectory=" + Files.isDirectory(parent)
                    + " isRegularFile=" + Files.isRegularFile(parent)
                    + " isSymbolicLink=" + Files.isSymbolicLink(parent)
                    + " fileExists=" + parent.toFile().exists()
                    + " fileIsDirectory=" + parent.toFile().isDirectory();
        } catch (Throwable throwable) {
            return "state inspection failed: " + throwable;
        }
    }

    private static final class ScratchpadInputStream extends InputStream {
        private final RandomAccessFile file;
        private final long end;
        private long mark = -1L;
        private long markLimit = 0L;
        private boolean closed;

        private ScratchpadInputStream(RandomAccessFile file, long offset, long length) throws IOException {
            this.file = file;
            this.end = endPosition(offset, length);
            file.seek(offset);
        }

        @Override
        public int read() throws IOException {
            ensureOpen();
            if (file.getFilePointer() >= end) {
                return -1;
            }
            return file.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            ensureOpen();
            Objects.checkFromIndexSize(off, len, b.length);
            if (len == 0) {
                return 0;
            }
            long remaining = end - file.getFilePointer();
            if (remaining <= 0L) {
                return -1;
            }
            return file.read(b, off, (int) Math.min(len, remaining));
        }

        @Override
        public int available() throws IOException {
            ensureOpen();
            long remaining = Math.max(0L, Math.min(file.length(), end) - file.getFilePointer());
            return (int) Math.min(remaining, Integer.MAX_VALUE);
        }

        @Override
        public void mark(int readlimit) {
            if (closed) {
                return;
            }
            try {
                mark = file.getFilePointer();
                markLimit = Math.max(0L, readlimit);
            } catch (IOException e) {
                mark = -1L;
                markLimit = 0L;
            }
        }

        @Override
        public void reset() throws IOException {
            ensureOpen();
            if (mark < 0L || file.getFilePointer() - mark > markLimit) {
                mark = -1L;
                markLimit = 0L;
                throw new IOException("Resetting to invalid mark");
            }
            file.seek(mark);
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                closed = true;
                mark = -1L;
                markLimit = 0L;
                file.close();
            }
        }

        private void ensureOpen() throws IOException {
            if (closed) {
                throw new IOException("Stream closed");
            }
        }

        private static long endPosition(long offset, long length) {
            if (length < 0L || Long.MAX_VALUE - offset < length) {
                return Long.MAX_VALUE;
            }
            return offset + length;
        }
    }

    private static final class RandomAccessFileOutputStream extends OutputStream {
        private final RandomAccessFile file;

        private RandomAccessFileOutputStream(RandomAccessFile file) {
            this.file = file;
        }

        @Override
        public void write(int b) throws IOException {
            file.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            file.write(b, off, len);
        }
    }

    private static final class BoundedOutputStream extends OutputStream {
        private final OutputStream delegate;
        private long remaining;

        private BoundedOutputStream(OutputStream delegate, long remaining) {
            this.delegate = delegate;
            this.remaining = Math.max(0L, remaining);
        }

        @Override
        public void write(int b) throws IOException {
            requireCapacity(1);
            delegate.write(b);
            remaining--;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            requireCapacity(len);
            delegate.write(b, off, len);
            remaining -= len;
        }

        private void requireCapacity(int amount) throws IOException {
            if (amount < 0 || remaining < amount) {
                throw new IOException("Scratchpad write exceeds declared length");
            }
        }
    }
}
