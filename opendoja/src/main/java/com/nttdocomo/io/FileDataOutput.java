package com.nttdocomo.io;

import opendoja.host.DoJaEncoding;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Data output for writing a file with random access.
 */
public class FileDataOutput implements java.io.DataOutput, RandomAccessible, AutoCloseable {
    private final FileEntity owner;
    private final RandomAccessFile file;
    private boolean closed;

    FileDataOutput(FileEntity owner, Path path) throws IOException {
        this.owner = owner;
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
        this.file = new RandomAccessFile(path.toFile(), "rw");
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
     * Gets the current access position of this data output.
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
     * Sets the absolute access position of this data output.
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
     * Truncates the file to the specified size.
     *
     * @param size the size to keep
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     * @throws IllegalArgumentException if {@code size} is negative
     */
    public void truncate(long size) throws IOException {
        ensureOpen();
        if (size < 0L) {
            throw new IllegalArgumentException("size out of range: " + size);
        }
        file.setLength(size);
        if (file.getFilePointer() > size) {
            file.seek(size);
        }
    }

    /**
     * Flushes buffered output to the file.
     *
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    public void flush() throws IOException {
        ensureOpen();
        file.getFD().sync();
    }

    /**
     * Closes the data output.
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
            owner.releaseDataOutput();
        }
    }

    /**
     * Converts a string to a byte sequence using the default encoding and
     * writes it.
     * First, the length of the converted byte sequence is written as two bytes
     * in the same manner as {@link #writeShort(int)}, then the byte sequence is
     * written.
     *
     * @param s the string to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     * @throws NullPointerException if {@code s} is {@code null}
     * @throws IllegalArgumentException if the converted byte length is 65536 or
     *         greater
     */
    public void writeString(String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("s");
        }
        byte[] data = s.getBytes(DoJaEncoding.DEFAULT_CHARSET);
        if (data.length >= 65536) {
            throw new IllegalArgumentException("encoded string is too large");
        }
        writeShort(data.length);
        write(data);
    }

    /**
     * Converts a string to a byte sequence using the default encoding and
     * writes it into a region of the specified size.
     * If the byte-sequence length is shorter than {@code bytes}, the remainder
     * of the region is filled with {@code 0x00}.
     *
     * @param s the string to write
     * @param bytes the size of the destination byte region
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     * @throws NullPointerException if {@code s} is {@code null}
     * @throws IllegalArgumentException if {@code bytes} is negative, is 65536 or
     *         greater, or is smaller than the converted byte-sequence length
     */
    public void writeString(String s, int bytes) throws IOException {
        if (s == null) {
            throw new NullPointerException("s");
        }
        if (bytes < 0 || bytes >= 65536) {
            throw new IllegalArgumentException("bytes out of range: " + bytes);
        }
        byte[] data = s.getBytes(DoJaEncoding.DEFAULT_CHARSET);
        if (data.length > bytes) {
            throw new IllegalArgumentException("encoded string is larger than bytes");
        }
        writeShort(data.length);
        write(data);
        for (int i = data.length; i < bytes; i++) {
            writeByte(0);
        }
    }

    /**
     * Writes the specified range of the byte array.
     *
     * @param b the source array
     * @param off the start offset in the array
     * @param len the number of bytes to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        file.write(b, off, len);
    }

    /**
     * Writes the specified byte array.
     *
     * @param b the source array
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void write(byte[] b) throws IOException {
        ensureOpen();
        file.write(b);
    }

    /**
     * Writes the low-order eight bits of the specified integer value as one
     * byte.
     *
     * @param b the integer value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        file.write(b);
    }

    /**
     * Writes the specified {@code boolean} value.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeBoolean(boolean v) throws IOException {
        ensureOpen();
        file.writeBoolean(v);
    }

    /**
     * Writes the low-order eight bits of the specified integer value as one
     * byte.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeByte(int v) throws IOException {
        ensureOpen();
        file.writeByte(v);
    }

    /**
     * Writes each character of the specified string as one byte per character.
     * For the detailed behavior, refer to the method of the same name in the
     * J2SE {@link java.io.DataOutput} interface.
     *
     * @param s the string to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeBytes(String s) throws IOException {
        ensureOpen();
        file.writeBytes(s);
    }

    /**
     * Writes the specified integer value as a {@code char}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeChar(int v) throws IOException {
        ensureOpen();
        file.writeChar(v);
    }

    /**
     * Writes each character of the specified string as two bytes per
     * character.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param s the string to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeChars(String s) throws IOException {
        ensureOpen();
        file.writeChars(s);
    }

    /**
     * Writes the specified {@code double} value.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeDouble(double v) throws IOException {
        ensureOpen();
        file.writeDouble(v);
    }

    /**
     * Writes the specified {@code float} value.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeFloat(float v) throws IOException {
        ensureOpen();
        file.writeFloat(v);
    }

    /**
     * Writes the specified {@code int} value.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeInt(int v) throws IOException {
        ensureOpen();
        file.writeInt(v);
    }

    /**
     * Writes the specified {@code long} value.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeLong(long v) throws IOException {
        ensureOpen();
        file.writeLong(v);
    }

    /**
     * Writes the specified integer value as a {@code short}.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param v the value to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeShort(int v) throws IOException {
        ensureOpen();
        file.writeShort(v);
    }

    /**
     * Writes the specified string together with its length information using
     * modified UTF-8 encoding.
     * For the detailed behavior, refer to the method of the same name in the
     * CLDC {@link java.io.DataOutput} interface.
     *
     * @param s the string to write
     * @throws IOException if this object or the owning {@link FileEntity} is
     *         already closed, or if an I/O error occurs
     */
    @Override
    public void writeUTF(String s) throws IOException {
        ensureOpen();
        file.writeUTF(s);
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("FileDataOutput is closed");
        }
        owner.ensureStillOpen();
    }
}
