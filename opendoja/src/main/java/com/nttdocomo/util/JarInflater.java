package com.nttdocomo.util;

import opendoja.host.DoJaEncoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A class for inflating and retrieving entries from a JAR-format file image.
 * To obtain a {@code JarInflater} object, call a constructor with an
 * {@link InputStream} or a byte array that holds a JAR-format file image.
 * To retrieve the contents of an entry, specify the entry name and call
 * {@link #getInputStream(String)}.
 *
 * <p>The stream or byte array specified to a constructor must yield exactly
 * one JAR file image with nothing missing and no extra data appended.
 * If part of the data is missing, or if extra trailing data is read, it may be
 * treated as a format violation and an exception may occur.
 * Even if the JAR file is signed, the signature is not verified.</p>
 *
 * <p>Entry names are decoded as UTF-8 when the ZIP archive marks them as such;
 * otherwise the host DoJa charset is used so legacy Shift_JIS-authored
 * archives remain loadable.</p>
 *
 * <p>Introduced in DoJa-3.0 (505i).</p>
 */
public class JarInflater {
    private final Map<String, byte[]> entries = new HashMap<>();
    private final Set<String> directories = new HashSet<>();
    private boolean closed;

    /**
     * Creates a new {@code JarInflater} from an input stream.
     * Exactly one valid JAR-format file image is read from the specified input
     * stream.
     * After the constructor finishes, no reference to the input stream is kept,
     * and the stream is not closed.
     *
     * @param inputStream the input stream holding a JAR-format file image
     * @throws NullPointerException if {@code inputStream} is {@code null}
     * @throws JarFormatException if a JAR-format error occurs
     * @throws IOException if an I/O error occurs while reading the data
     */
    public JarInflater(InputStream inputStream) throws JarFormatException, IOException {
        this(readAllBytes(inputStream));
    }

    /**
     * Creates a new {@code JarInflater} from a byte array.
     * One valid JAR-format file image is read from the beginning of the byte
     * array.
     * After the constructor finishes, no reference to the byte array is kept.
     *
     * @param data the byte array holding a JAR-format file image
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws JarFormatException if a JAR-format error occurs
     */
    public JarInflater(byte[] data) throws JarFormatException {
        if (data == null) {
            throw new NullPointerException("data");
        }
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data), DoJaEncoding.defaultCharset())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    directories.add(normalizeDirectoryName(entry.getName()));
                    continue;
                }
                entries.put(entry.getName(), readAllBytes(zip));
            }
        } catch (IOException e) {
            throw new JarFormatException(e.getMessage());
        }
    }

    /**
     * Closes this {@code JarInflater}.
     * Reading from input streams already obtained from this {@code JarInflater}
     * is not guaranteed after this call.
     */
    public void close() {
        entries.clear();
        directories.clear();
        closed = true;
    }

    /**
     * Returns the size of the specified entry after inflation.
     *
     * @param name the entry name
     * @return the inflated size of the found entry; if the found entry is a
     *         directory, returns {@code 0}; if the entry is not found, returns
     *         {@code -1}
     * @throws IllegalStateException if this {@code JarInflater} has been closed
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws JarFormatException if a JAR-format error occurs
     */
    public long getSize(String name) throws JarFormatException {
        ensureOpen();
        if (name == null) {
            throw new NullPointerException("name");
        }
        byte[] data = entries.get(name);
        if (data != null) {
            return data.length;
        }
        return directories.contains(normalizeDirectoryName(name)) ? 0 : -1;
    }

    /**
     * Returns an input stream for reading the specified entry.
     * The data obtained from the input stream is the inflated data.
     * A different {@code InputStream} object is returned for every call.
     *
     * @param name the entry name
     * @return an input stream for reading the inflated entry data; if the
     *         specified entry is a directory or is not found, returns
     *         {@code null}
     * @throws IllegalStateException if this {@code JarInflater} has been closed
     * @throws NullPointerException if {@code name} is {@code null}
     * @throws IllegalArgumentException if {@code name} contains {@code "/./"}
     *         or {@code "/../"}
     * @throws JarFormatException if a JAR-format error occurs
     */
    public InputStream getInputStream(String name) throws JarFormatException {
        ensureOpen();
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (name.contains("/./") || name.contains("/../")) {
            throw new IllegalArgumentException("name");
        }
        byte[] data = entries.get(name);
        if (data == null) {
            return null;
        }
        return new ByteArrayInputStream(data);
    }

    private static byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) >= 0) {
            if (read == 0) {
                continue;
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static String normalizeDirectoryName(String name) {
        return name.endsWith("/") ? name : name + "/";
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("JarInflater is closed");
        }
    }
}
