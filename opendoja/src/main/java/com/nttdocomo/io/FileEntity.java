package com.nttdocomo.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a file that can be opened as DoJa data-input/data-output objects
 * or as byte streams.
 */
public class FileEntity {
    private final Path path;
    private final boolean readable;
    private final boolean writable;
    private final Runnable closeHook;

    private boolean closed;
    private int bufferSize = 8192;
    private OpenKind openKind = OpenKind.NONE;
    private AutoCloseable activeAccessor;

    /**
     * Creates a file object with the specified path and open mode.
     *
     * @param path the file path managed by this entity
     * @param readable {@code true} if reading is allowed
     * @param writable {@code true} if writing is allowed
     */
    public FileEntity(Path path, boolean readable, boolean writable) {
        this(path, readable, writable, null);
    }

    /**
     * Creates a file object with the specified path, open mode, and close
     * hook.
     *
     * @param path the file path managed by this entity
     * @param readable {@code true} if reading is allowed
     * @param writable {@code true} if writing is allowed
     * @param closeHook the action executed when this entity is closed, or
     *                  {@code null}
     */
    public FileEntity(Path path, boolean readable, boolean writable, Runnable closeHook) {
        this.path = Objects.requireNonNull(path, "path");
        this.readable = readable;
        this.writable = writable;
        this.closeHook = closeHook;
    }

    /**
     * Closes the file.
     * If the file is already closed, nothing happens.
     *
     * @throws IOException if an I/O error occurs
     */
    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        IOException failure = null;
        AutoCloseable accessor = activeAccessor;
        activeAccessor = null;
        if (accessor != null) {
            try {
                accessor.close();
            } catch (IOException exception) {
                failure = exception;
            } catch (Exception exception) {
                failure = new IOException("Failed to close file accessor", exception);
            }
        }
        if (closeHook != null) {
            closeHook.run();
        }
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Sets the buffer size.
     *
     * @param bufferSize the buffer size
     * @throws RuntimeException if this object is already closed
     * @throws IllegalArgumentException if {@code bufferSize} is negative
     * @throws IOException if an I/O error occurs
     */
    public void setBufferSize(int bufferSize) throws IOException {
        ensureOpenRuntime();
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize out of range: " + bufferSize);
        }
        this.bufferSize = bufferSize;
    }

    /**
     * Gets the buffer size actually in use.
     *
     * @return the buffer size
     * @throws RuntimeException if this object is already closed
     * @throws IOException if an I/O error occurs
     */
    public int getBufferSize() throws IOException {
        ensureOpenRuntime();
        return bufferSize;
    }

    /**
     * Opens and returns a data-input instance for reading the file.
     * This method is mutually exclusive with {@link #openInputStream()} and
     * {@link #openOutputStream()}.
     *
     * @return the data-input instance
     * @throws RuntimeException if this object is already closed
     * @throws ConnectionException if the file was opened write-only or another
     *         accessor is already open
     * @throws IOException if an I/O error occurs
     */
    public FileDataInput openDataInput() throws IOException {
        synchronized (this) {
            ensureOpenRuntime();
            if (!readable) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File is write-only");
            }
            if (openKind != OpenKind.NONE) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File already has an open accessor");
            }
            openKind = OpenKind.DATA_INPUT;
        }
        try {
            FileDataInput dataInput = new FileDataInput(this, path);
            synchronized (this) {
                activeAccessor = dataInput;
            }
            return dataInput;
        } catch (IOException exception) {
            synchronized (this) {
                openKind = OpenKind.NONE;
            }
            throw exception;
        }
    }

    /**
     * Opens and returns a data-output instance for writing the file.
     * This method is mutually exclusive with {@link #openInputStream()} and
     * {@link #openOutputStream()}.
     *
     * @return the data-output instance
     * @throws RuntimeException if this object is already closed
     * @throws ConnectionException if the file was opened read-only or another
     *         accessor is already open
     * @throws IOException if an I/O error occurs
     */
    public FileDataOutput openDataOutput() throws IOException {
        synchronized (this) {
            ensureOpenRuntime();
            if (!writable) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File is read-only");
            }
            if (openKind != OpenKind.NONE) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File already has an open accessor");
            }
            openKind = OpenKind.DATA_OUTPUT;
        }
        try {
            FileDataOutput dataOutput = new FileDataOutput(this, path);
            synchronized (this) {
                activeAccessor = dataOutput;
            }
            return dataOutput;
        } catch (IOException exception) {
            synchronized (this) {
                openKind = OpenKind.NONE;
            }
            throw exception;
        }
    }

    /**
     * Opens and returns an input stream for reading the file.
     * This method is mutually exclusive with {@link #openDataInput()} and
     * {@link #openDataOutput()}.
     *
     * @return the input stream
     * @throws RuntimeException if this object is already closed
     * @throws ConnectionException if the file was opened write-only or another
     *         accessor is already open
     * @throws IOException if an I/O error occurs
     */
    public InputStream openInputStream() throws IOException {
        synchronized (this) {
            ensureOpenRuntime();
            if (!readable) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File is write-only");
            }
            if (openKind != OpenKind.NONE) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File already has an open accessor");
            }
            openKind = OpenKind.INPUT_STREAM;
        }
        ensureFileExists();
        ManagedInputStream stream = new ManagedInputStream(Files.newInputStream(path));
        synchronized (this) {
            activeAccessor = stream;
        }
        return stream;
    }

    /**
     * Opens and returns an output stream for writing the file.
     * This method is mutually exclusive with {@link #openDataInput()} and
     * {@link #openDataOutput()}.
     *
     * @return the output stream
     * @throws RuntimeException if this object is already closed
     * @throws ConnectionException if the file was opened read-only or another
     *         accessor is already open
     * @throws IOException if an I/O error occurs
     */
    public OutputStream openOutputStream() throws IOException {
        synchronized (this) {
            ensureOpenRuntime();
            if (!writable) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File is read-only");
            }
            if (openKind != OpenKind.NONE) {
                throw new ConnectionException(ConnectionException.ILLEGAL_STATE,
                        "File already has an open accessor");
            }
            openKind = OpenKind.OUTPUT_STREAM;
        }
        ensureFileExists();
        ManagedOutputStream stream = new ManagedOutputStream(Files.newOutputStream(path));
        synchronized (this) {
            activeAccessor = stream;
        }
        return stream;
    }

    synchronized void releaseDataInput() {
        activeAccessor = null;
        release(OpenKind.DATA_INPUT);
    }

    synchronized void releaseDataOutput() {
        activeAccessor = null;
        release(OpenKind.DATA_OUTPUT);
    }

    synchronized void releaseInputStream() {
        activeAccessor = null;
        release(OpenKind.INPUT_STREAM);
    }

    synchronized void releaseOutputStream() {
        activeAccessor = null;
        release(OpenKind.OUTPUT_STREAM);
    }

    private synchronized void release(OpenKind kind) {
        if (openKind == kind) {
            openKind = OpenKind.NONE;
        }
    }

    synchronized void ensureStillOpen() throws IOException {
        if (closed) {
            throw new IOException("FileEntity is closed");
        }
    }

    private synchronized void ensureOpenRuntime() {
        if (closed) {
            throw new RuntimeException("FileEntity is closed");
        }
    }

    private void ensureFileExists() throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    private enum OpenKind {
        NONE,
        DATA_INPUT,
        DATA_OUTPUT,
        INPUT_STREAM,
        OUTPUT_STREAM
    }

    private final class ManagedInputStream extends FilterInputStream {
        private ManagedInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                releaseInputStream();
            }
        }
    }

    private final class ManagedOutputStream extends FilterOutputStream {
        private ManagedOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                releaseOutputStream();
            }
        }
    }
}
