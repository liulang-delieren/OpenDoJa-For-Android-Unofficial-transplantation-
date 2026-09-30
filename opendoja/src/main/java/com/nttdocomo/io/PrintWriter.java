package com.nttdocomo.io;

import opendoja.host.DoJaEncoding;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A writer that formats data in a convenient text form.
 */
public class PrintWriter extends Writer {
    private static final String LINE_SEPARATOR = "\r\n";

    /**
     * The {@code Writer} used as the output destination of this
     * {@code PrintWriter}.
     */
    protected Writer out;

    private final boolean autoFlush;
    private boolean error;
    private boolean closed;

    /**
     * Creates a {@code PrintWriter} that writes to the specified
     * {@code Writer} and does not perform automatic line flushing.
     *
     * @param out the destination {@code Writer}
     * @throws NullPointerException if {@code out} is {@code null}
     */
    public PrintWriter(Writer out) {
        this(out, false);
    }

    /**
     * Creates a {@code PrintWriter} that writes to the specified
     * {@code Writer}.
     *
     * @param out the destination {@code Writer}
     * @param autoFlush if {@code true}, the {@code println} methods flush the
     *        stream
     * @throws NullPointerException if {@code out} is {@code null}
     */
    public PrintWriter(Writer out, boolean autoFlush) {
        if (out == null) {
            throw new NullPointerException("out");
        }
        this.out = out;
        this.autoFlush = autoFlush;
    }

    /**
     * Creates a {@code PrintWriter} that writes to the specified
     * {@code OutputStream} and does not perform automatic line flushing.
     * The automatically created {@code OutputStreamWriter} uses the default
     * DoJa encoding.
     *
     * @param out the destination {@code OutputStream}
     * @throws NullPointerException if {@code out} is {@code null}
     */
    public PrintWriter(OutputStream out) {
        this(out, false);
    }

    /**
     * Creates a {@code PrintWriter} that writes to the specified
     * {@code OutputStream}.
     * The automatically created {@code OutputStreamWriter} uses the default
     * DoJa encoding.
     *
     * @param out the destination {@code OutputStream}
     * @param autoFlush if {@code true}, the {@code println} methods flush the
     *        stream
     * @throws NullPointerException if {@code out} is {@code null}
     */
    public PrintWriter(OutputStream out, boolean autoFlush) {
        this(new OutputStreamWriter(out, DoJaEncoding.DEFAULT_CHARSET), autoFlush);
    }

    /**
     * Flushes the stream.
     *
     * @see #checkError()
     */
    @Override
    public void flush() {
        if (out == null) {
            setError();
            return;
        }
        try {
            out.flush();
        } catch (IOException exception) {
            setError();
        }
    }

    /**
     * Closes the stream.
     * If a stream that is already closed is closed again, nothing happens.
     *
     * @see #checkError()
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (out == null) {
            return;
        }
        try {
            out.close();
        } catch (IOException exception) {
            setError();
        } finally {
            out = null;
        }
    }

    /**
     * Checks the error status of the stream.
     * If the stream is not closed, the stream is flushed before checking the
     * error status.
     * Once an error occurs, this method always returns {@code true}
     * thereafter.
     *
     * @return {@code true} if an error has occurred; otherwise {@code false}
     * @see #setError()
     */
    public boolean checkError() {
        if (!closed) {
            flush();
        }
        return error;
    }

    /**
     * Records that an error has occurred.
     *
     * @see #checkError()
     */
    protected void setError() {
        error = true;
    }

    /**
     * Writes one character.
     *
     * @param c the character to write
     * @see #checkError()
     */
    @Override
    public void write(int c) {
        writeInternal(() -> out.write(c));
    }

    /**
     * Writes part of a character array.
     *
     * @param buf the source array
     * @param off the start offset in the array
     * @param len the number of characters to write
     * @see #checkError()
     */
    @Override
    public void write(char[] buf, int off, int len) {
        writeInternal(() -> out.write(buf, off, len));
    }

    /**
     * Writes a character array.
     *
     * @param buf the source array
     * @throws NullPointerException if {@code buf} is {@code null}
     * @see #checkError()
     */
    @Override
    public void write(char[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf");
        }
        write(buf, 0, buf.length);
    }

    /**
     * Writes part of a string.
     *
     * @param s the string to write
     * @param off the start offset in the string
     * @param len the number of characters to write
     * @see #checkError()
     */
    @Override
    public void write(String s, int off, int len) {
        writeInternal(() -> out.write(s, off, len));
    }

    /**
     * Writes a string.
     *
     * @param s the string to write
     * @throws NullPointerException if {@code s} is {@code null}
     * @see #checkError()
     */
    @Override
    public void write(String s) {
        if (s == null) {
            throw new NullPointerException("s");
        }
        write(s, 0, s.length());
    }

    /**
     * Prints a {@code boolean} value.
     *
     * @param b the {@code boolean} value to print
     * @see #checkError()
     */
    public void print(boolean b) {
        writeInternal(() -> out.write(String.valueOf(b)));
    }

    /**
     * Prints a character.
     *
     * @param c the character to print
     * @see #checkError()
     */
    public void print(char c) {
        write(c);
    }

    /**
     * Prints an {@code int} value.
     *
     * @param i the {@code int} value to print
     * @see #checkError()
     */
    public void print(int i) {
        writeInternal(() -> out.write(String.valueOf(i)));
    }

    /**
     * Prints a {@code long} value.
     *
     * @param l the {@code long} value to print
     * @see #checkError()
     */
    public void print(long l) {
        writeInternal(() -> out.write(String.valueOf(l)));
    }

    /**
     * Prints a character array.
     *
     * @param s the character array to print
     * @throws NullPointerException if {@code s} is {@code null}
     * @see #checkError()
     */
    public void print(char[] s) {
        if (s == null) {
            throw new NullPointerException("s");
        }
        write(s);
    }

    /**
     * Prints a string.
     * If {@code s} is {@code null}, the string {@code "null"} is printed.
     *
     * @param s the string to print
     * @see #checkError()
     */
    public void print(String s) {
        writeInternal(() -> out.write(String.valueOf(s)));
    }

    /**
     * Prints an object.
     * The string generated by {@link String#valueOf(Object)} is printed.
     *
     * @param obj the object to print
     * @see #checkError()
     */
    public void print(Object obj) {
        writeInternal(() -> out.write(String.valueOf(obj)));
    }

    /**
     * Prints the line-separator string.
     * The line separator is CRLF ({@code "\r\n"}).
     *
     * @see #checkError()
     */
    public void println() {
        writeInternal(() -> out.write(LINE_SEPARATOR));
        if (autoFlush) {
            flush();
        }
    }

    /**
     * Prints a {@code boolean} value and the line separator.
     *
     * @param b the {@code boolean} value to print
     * @see #checkError()
     */
    public void println(boolean b) {
        print(b);
        println();
    }

    /**
     * Prints a character and the line separator.
     *
     * @param c the character to print
     * @see #checkError()
     */
    public void println(char c) {
        print(c);
        println();
    }

    /**
     * Prints an {@code int} value and the line separator.
     *
     * @param i the {@code int} value to print
     * @see #checkError()
     */
    public void println(int i) {
        print(i);
        println();
    }

    /**
     * Prints a {@code long} value and the line separator.
     *
     * @param l the {@code long} value to print
     * @see #checkError()
     */
    public void println(long l) {
        print(l);
        println();
    }

    /**
     * Prints a character array and the line separator.
     *
     * @param s the character array to print
     * @throws NullPointerException if {@code s} is {@code null}
     * @see #checkError()
     */
    public void println(char[] s) {
        print(s);
        println();
    }

    /**
     * Prints a string and the line separator.
     *
     * @param s the string to print
     * @see #checkError()
     */
    public void println(String s) {
        print(s);
        println();
    }

    /**
     * Prints an object and the line separator.
     *
     * @param obj the object to print
     * @see #checkError()
     */
    public void println(Object obj) {
        print(obj);
        println();
    }

    private void writeInternal(IoRunnable action) {
        if (out == null) {
            setError();
            return;
        }
        try {
            action.run();
        } catch (IOException exception) {
            setError();
        }
    }

    @FunctionalInterface
    private interface IoRunnable {
        void run() throws IOException;
    }
}
