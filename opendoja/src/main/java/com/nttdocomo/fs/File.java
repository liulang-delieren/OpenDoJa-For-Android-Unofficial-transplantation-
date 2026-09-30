package com.nttdocomo.fs;

import com.nttdocomo.io.FileEntity;
import opendoja.host.storage.DoJaStorageHost;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Defines a file.
 * Instances of this class hold the path name to a file and perform operations
 * on that file in the storage device at the time each method is called.
 */
public class File {
    /** Mode indicating that the file is opened read-only (=0). */
    public static final int MODE_READ_ONLY = 0;

    /** Mode indicating that the file is opened write-only (=1). */
    public static final int MODE_WRITE_ONLY = 1;

    /** Mode indicating that the file is opened for reading and writing (=2). */
    public static final int MODE_READ_WRITE = 2;

    private final Folder folder;
    private final String fileName;
    private final String path;
    private final Path backingPath;

    private File(Folder folder, String fileName) {
        this(folder, fileName, folder.backingPath().resolve(fileName));
    }

    private File(Folder folder, String fileName, Path backingPath) {
        this.folder = folder;
        this.fileName = fileName;
        this.backingPath = backingPath;
        this.path = "/".equals(folder.getPath()) ? "/" + fileName : folder.getPath() + "/" + fileName;
    }

    static File of(Folder folder, String fileName, Path backingPath) {
        return new File(folder, fileName, backingPath);
    }

    /**
     * Gets the source folder instance of this file.
     *
     * @return the source folder instance of this file
     */
    public Folder getFolder() {
        return folder;
    }

    /**
     * Gets the access right to this file.
     *
     * @return the access right to this file
     */
    public AccessToken getAccessToken() {
        return folder.getAccessToken();
    }

    /**
     * Gets the absolute path name of this file.
     *
     * @return the absolute path name of this file
     */
    public String getPath() {
        return path;
    }

    /**
     * Opens the file.
     *
     * @param mode the open mode; one of {@link #MODE_READ_ONLY},
     *        {@link #MODE_WRITE_ONLY}, or {@link #MODE_READ_WRITE}
     * @return the file entity
     * @throws IllegalArgumentException if {@code mode} is invalid
     * @throws IOException if the file cannot be accessed
     */
    public FileEntity open(int mode) throws IOException {
        if (mode != MODE_READ_ONLY && mode != MODE_WRITE_ONLY && mode != MODE_READ_WRITE) {
            throw new IllegalArgumentException("Invalid file open mode: " + mode);
        }
        ensureExistingFile();
        boolean readable = mode != MODE_WRITE_ONLY;
        boolean writable = mode != MODE_READ_ONLY;
        if (readable && !Files.isReadable(backingPath)) {
            throw new FileNotAccessibleException(FileNotAccessibleException.ACCESS_DENIED,
                    "File is not readable: " + fileName);
        }
        if (writable && !Files.isWritable(backingPath)) {
            throw new FileNotAccessibleException(FileNotAccessibleException.ACCESS_DENIED,
                    "File is not writable: " + fileName);
        }
        Runnable release = DoJaStorageHost.acquireOpen(backingPath, mode);
        try {
            return new FileEntity(backingPath, readable, writable, release);
        } catch (RuntimeException exception) {
            release.run();
            throw exception;
        }
    }

    /**
     * Deletes the file.
     *
     * @throws IOException if the file cannot be deleted
     */
    public void delete() throws IOException {
        ensureExistingFile();
        if (DoJaStorageHost.isFileOpen(backingPath)) {
            throw new FileNotAccessibleException(FileNotAccessibleException.IN_USE,
                    "File is open: " + fileName);
        }
        try {
            Files.delete(backingPath);
        } catch (IOException exception) {
            throw DoJaStorageHost.translateDeleteFailure(exception);
        }
    }

    /**
     * Gets the file size in bytes.
     *
     * @return the file size in bytes
     * @throws IOException if the file cannot be accessed
     */
    public long getLength() throws IOException {
        ensureExistingFile();
        try {
            return Files.size(backingPath);
        } catch (IOException exception) {
            throw DoJaStorageHost.translateExistingPathFailure(exception);
        }
    }

    /**
     * Gets the last-modified time of the file.
     *
     * @return the difference in milliseconds between the file's last-modified
     *         time and January 1, 1970 00:00:00 GMT
     * @throws IOException if the file cannot be accessed
     */
    public long getLastModified() throws IOException {
        ensureExistingFile();
        try {
            return Files.getLastModifiedTime(backingPath).toMillis();
        } catch (IOException exception) {
            throw DoJaStorageHost.translateExistingPathFailure(exception);
        }
    }

    private void ensureExistingFile() throws IOException {
        if (!Files.exists(backingPath)) {
            throw new FileNotAccessibleException(FileNotAccessibleException.NOT_FOUND,
                    "File not found: " + fileName);
        }
        if (!Files.isRegularFile(backingPath)) {
            throw new FileNotAccessibleException(FileNotAccessibleException.ACCESS_DENIED,
                    "Path does not refer to a file: " + fileName);
        }
    }
}
