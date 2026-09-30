package com.nttdocomo.system;

import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Represents one native movie-data entry.
 */
public final class MovieStore {
    private final int id;
    private final byte[] bytes;
    private final MediaImage image;

    MovieStore(_SystemSupport.ImageEntry entry) {
        this.id = entry.id;
        this.bytes = entry.image.bytes.clone();
        this.image = MediaManager.getImage(this.bytes);
    }

    /**
     * Registers movie data.
     *
     * @param movie the movie-data media image
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(MediaImage movie) throws InterruptedOperationException {
        return _SystemSupport.addMovie(movie);
    }

    /**
     * Gets a movie entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static MovieStore getEntry(int id) throws StoreException {
        return _SystemSupport.getMovieEntry(id);
    }

    /**
     * Gets movie entry IDs contained directly under the specified folder.
     *
     * @param folderId the folder entry ID
     * @return the entry-ID list, or {@code null}
     * @throws StoreException if the folder does not exist
     */
    public static int[] getEntryIds(int folderId) throws StoreException {
        return _SystemSupport.getMovieEntryIds(folderId);
    }

    /**
     * Gets the entry ID.
     *
     * @return the entry ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the media image.
     *
     * @return the media image
     */
    public MediaImage getImage() {
        return image;
    }

    /**
     * Gets an input stream for the encoded file image.
     *
     * @return a new input stream for the encoded bytes
     */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes.clone());
    }

    /**
     * Obtains a movie-data entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static MovieStore selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectMovieEntry();
    }
}
