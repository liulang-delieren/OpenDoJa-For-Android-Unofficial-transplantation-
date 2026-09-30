package com.nttdocomo.io;

import opendoja.host.DoJaEncoding;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data input for reading a file with random access.
 */
public class FileDataInput implements java.io.DataInput, RandomAccessible, AutoCloseable {
    private final FileEntity owner;
    private final RandomAccessFile file;
    private boolean closed;

    FileDataInput(FileEntity owner, Path path) throws IOException {
        this.owner = owner;
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        this.file = new RandomAccessFile(path.toFile(), "r");
    }

    /**
     * Gets the size of the file.
     *
     * @return the file size
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public long getSize() throws IOException {
        ensureOpen();
        return file.length();
    }

    /**
     * Gets the current access position of this data input.
     *
     * @return the current access position
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public long getPosition() throws IOException {
        ensureOpen();
        return file.getFilePointer();
    }

    /**
     * Sets the absolute access position of this data input.
     *
     * @param position the access position
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     * @throws IllegalArgumentException if {@code position} is negative
     */
    @Override
    public void setPosition(long position) throws IOException {
        ensureOpen();
        if (position < 0L) {
            throw new IllegalArgumentException("position out of range: " + position);
        }
        file.seek(position);
    }

    /**
     * Sets the access position relative to the current position.
     *
     * @param position the relative position
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     * @throws IllegalArgumentException if the resulting absolute position would
     *         be less than 0
     */
    @Override
    public void setPositionRelative(long position) throws IOException {
        ensureOpen();
        long next = file.getFilePointer() + position;
        if (next < 0L) {
            throw new IllegalArgumentException("position out of range: " + position);
        }
        file.seek(next);
    }

    /**
     * Closes the data input.
     * If it is already closed, nothing happens.
     *
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        try {
            file.close();
        } finally {
            owner.releaseDataInput();
        }
    }

    /**
     * Reads a byte sequence as a string using the default encoding.
     * First, the byte-sequence length is read as two bytes in the same manner
     * as {@link #readUnsignedShort()}, then that many bytes are read and
     * converted to a string.
     *
     * @return the string that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    public String readString() throws IOException {
        int length = readUnsignedShort();
        byte[] data = new byte[length];
        readFully(data);
        return new String(data, DoJaEncoding.DEFAULT_CHARSET);
    }

    /**
     * Reads a byte sequence as a string using the default encoding.
     * After reading the two-byte length, if the length is greater than
     * {@code bytes}, an exception is thrown and the file position remains
     * advanced by only 2 bytes.
     *
     * @param bytes the size of the region in bytes
     * @return the string that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     * @throws IllegalArgumentException if {@code bytes} is negative, is 65536 or
     *         greater, or is smaller than the stored byte-sequence length
     */
    public String readString(int bytes) throws IOException {
        ensureOpen();
        if (bytes < 0 || bytes >= 65536) {
            throw new IllegalArgumentException("bytes out of range: " + bytes);
        }
        int length = readUnsignedShort();
        if (length > bytes) {
            throw new IllegalArgumentException("bytes is smaller than the stored byte length");
        }
        byte[] data = new byte[length];
        readFully(data);
        long padding = bytes - length;
        if (padding > 0) {
            setPositionRelative(padding);
        }
        return new String(data, DoJaEncoding.DEFAULT_CHARSET);
    }

    /**
     * Reads one byte and converts it to a {@code boolean} value.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code boolean} value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public boolean readBoolean() throws IOException {
        ensureOpen();
        return file.readBoolean();
    }

    /**
     * Reads one byte.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the byte that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public byte readByte() throws IOException {
        ensureOpen();
        return file.readByte();
    }

    /**
     * Reads two bytes and converts them to a {@code char}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code char} that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public char readChar() throws IOException {
        ensureOpen();
        return file.readChar();
    }

    /**
     * Reads eight bytes and converts them to a {@code double}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code double} value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public double readDouble() throws IOException {
        ensureOpen();
        return file.readDouble();
    }

    /**
     * Reads four bytes and converts them to a {@code float}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code float} value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public float readFloat() throws IOException {
        ensureOpen();
        return file.readFloat();
    }

    /**
     * Reads bytes completely into part of the specified array.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @param b the destination array
     * @param off the start offset in the array
     * @param len the number of bytes to read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        file.readFully(b, off, len);
    }

    /**
     * Reads bytes completely into the specified array.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @param b the destination array
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public void readFully(byte[] b) throws IOException {
        ensureOpen();
        file.readFully(b);
    }

    /**
     * Reads four bytes and converts them to an {@code int}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code int} value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public int readInt() throws IOException {
        ensureOpen();
        return file.readInt();
    }

    /**
     * Reads up to the next line break and converts it to a {@link String}.
     * For the detailed behavior, refer to the method of the same name in the
     * J2SE {@link java.io.DataInput} interface.
     *
     * @return the line that was read, or {@code null} at end of file
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public String readLine() throws IOException {
        ensureOpen();
        byte[] buffer = readLineBytes();
        return buffer == null ? null : new String(buffer, DoJaEncoding.DEFAULT_CHARSET);
    }

    /**
     * Reads eight bytes and converts them to a {@code long}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code long} value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public long readLong() throws IOException {
        ensureOpen();
        return file.readLong();
    }

    /**
     * Reads two bytes and converts them to a {@code short}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the {@code short} value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public short readShort() throws IOException {
        ensureOpen();
        return file.readShort();
    }

    /**
     * Reads one byte as an integer from {@code 0} through {@code 255}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the unsigned-byte value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public int readUnsignedByte() throws IOException {
        ensureOpen();
        return file.readUnsignedByte();
    }

    /**
     * Reads two bytes as an integer from {@code 0} through {@code 65535}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the unsigned-short value that was read
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public int readUnsignedShort() throws IOException {
        ensureOpen();
        return file.readUnsignedShort();
    }

    /**
     * Reads a byte sequence as a modified UTF-8 encoded string.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @return the decoded string
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, if the end of file is reached too early, or if an
     *         I/O error occurs
     */
    @Override
    public String readUTF() throws IOException {
        ensureOpen();
        return file.readUTF();
    }

    /**
     * Skips the specified number of bytes.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataInput} interface.
     *
     * @param n the number of bytes to skip
     * @return the number of bytes actually skipped
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public int skipBytes(int n) throws IOException {
        ensureOpen();
        return file.skipBytes(n);
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("FileDataInput is closed");
        }
        owner.ensureStillOpen();
    }

    private byte[] readLineBytes() throws IOException {
        long start = file.getFilePointer();
        int first = file.read();
        if (first < 0) {
            return null;
        }
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int current = first;
        while (current >= 0) {
            if (current == '\n') {
                break;
            }
            if (current == '\r') {
                long nextPos = file.getFilePointer();
                int next = file.read();
                if (next >= 0 && next != '\n') {
                    file.seek(nextPos);
                }
                break;
            }
            buffer.write(current);
            current = file.read();
        }
        if (buffer.size() == 0 && current < 0 && file.getFilePointer() == start + 1 && first < 0) {
            return null;
        }
        return buffer.toByteArray();
    }
}
