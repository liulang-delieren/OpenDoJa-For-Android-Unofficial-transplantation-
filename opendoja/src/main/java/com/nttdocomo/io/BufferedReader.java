package com.nttdocomo.io;

import java.io.IOException;
import java.io.Reader;

/**
 * A buffered character-input stream.
 */
public class BufferedReader extends Reader {
    private final java.io.BufferedReader delegate;

    /**
     * Creates a {@code BufferedReader} that uses the implementation's standard
     * buffer size.
     *
     * @param in the source stream of the {@code BufferedReader} to create
     * @throws NullPointerException if {@code in} is {@code null}
     */
    public BufferedReader(Reader in) {
        this.delegate = new java.io.BufferedReader(in);
    }

    /**
     * Creates a {@code BufferedReader} with the specified buffer size.
     *
     * @param in the source stream of the {@code BufferedReader} to create
     * @param size the buffer size in characters
     * @throws NullPointerException if {@code in} is {@code null}
     * @throws IllegalArgumentException if {@code size} is 0 or less
     */
    public BufferedReader(Reader in, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size out of range: " + size);
        }
        this.delegate = new java.io.BufferedReader(in, size);
    }

    /**
     * Reads one character.
     *
     * @return the read character as an {@code int} ({@code 0x0000-0xFFFF}),
     *         or {@code -1} if there is no character to return because the end
     *         of the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    /**
     * Reads characters into part of an array.
     *
     * @param buf the destination array
     * @param off the start offset within the array
     * @param len the maximum number of characters to read
     * @return the number of characters actually read, or {@code -1} if there is
     *         no character to return because the end of the stream has been
     *         reached
     * @throws NullPointerException if {@code buf} is {@code null}
     * @throws IndexOutOfBoundsException if {@code off} or {@code len} is
     *         negative, or if {@code off + len} exceeds the array length
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read(char[] buf, int off, int len) throws IOException {
        return delegate.read(buf, off, len);
    }

    /**
     * Reads one line.
     * The end of a line is determined by LF ({@code '\n'}), CR ({@code '\r'}),
     * or CRLF ({@code "\r\n"}).
     *
     * @return the line read, not including the line-termination characters, or
     *         {@code null} if a line cannot be read because the end of the
     *         stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException {
        return delegate.readLine();
    }

    /**
     * Skips characters.
     *
     * @param n the number of characters to skip
     * @return the number of characters actually skipped
     * @throws IllegalArgumentException if {@code n} is negative
     * @throws IOException if an I/O error occurs
     */
    @Override
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip out of range: " + n);
        }
        return delegate.skip(n);
    }

    /**
     * Tests whether the stream can be read.
     *
     * <p>DoJa titles often use this as an end-of-input probe before calling
     * {@link #readLine()}, especially on scratchpad-backed resources. Java's
     * {@link java.io.BufferedReader#ready()} only answers whether a read is
     * non-blocking, which can be {@code false} for readable file slices. To
     * preserve DoJa compatibility, peek one character ahead when the delegate
     * does not already report ready.
     *
     * @return {@code true} if another character can be read; otherwise
     *         {@code false}
     * @throws IOException if an I/O error occurs
     */
    @Override
    public boolean ready() throws IOException {
        if (delegate.ready()) {
            return true;
        }
        if (!delegate.markSupported()) {
            return false;
        }
        delegate.mark(1);
        int next = delegate.read();
        delegate.reset();
        return next >= 0;
    }

    /**
     * Tests whether mark operations are supported.
     * Because mark operations are supported, this method always returns
     * {@code true}.
     *
     * @return always {@code true}
     */
    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    /**
     * Marks the current position in the stream.
     * When {@link #reset()} is called, reading can be resumed from the marked
     * position.
     *
     * @param readAheadLimit the maximum number of characters that can be read
     *        while keeping the mark position
     * @throws IllegalArgumentException if {@code readAheadLimit} is negative
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("readAheadLimit out of range: " + readAheadLimit);
        }
        delegate.mark(readAheadLimit);
    }

    /**
     * Returns to the position marked most recently.
     *
     * @throws IOException if no mark has been set, the marked position cannot be
     *         restored, or an I/O error occurs
     */
    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    /**
     * Closes the stream.
     * If a stream that is already closed is closed again, nothing happens.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
