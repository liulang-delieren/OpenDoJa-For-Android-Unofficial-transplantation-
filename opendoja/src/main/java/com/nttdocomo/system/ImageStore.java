package com.nttdocomo.system;

import com.nttdocomo.ui.MediaImage;
import com.nttdocomo.ui.MediaManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Represents one native image-data entry.
 */
public final class ImageStore {
    /** Indicates that batch image registration is supported on the desktop host. */
    public static final boolean isSupportedAddImageArray = true;

    /** Indicates the maximum supported batch-registration length on the desktop host. */
    public static final int addImageArrayMaxLen = _SystemSupport.IMAGE_ARRAY_MAX_LEN;

    private final int id;
    private final byte[] bytes;
    private final MediaImage image;

    ImageStore(_SystemSupport.ImageEntry entry) {
        this.id = entry.id;
        this.bytes = entry.image.bytes.clone();
        this.image = MediaManager.getImage(this.bytes);
    }

    /**
     * Obtains an image-data entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static ImageStore selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectImageEntry();
    }

    /**
     * Obtains an image-data entry ID through native-style user selection.
     *
     * @return the selected entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int selectEntryId() throws InterruptedOperationException {
        return _SystemSupport.selectImageEntryId();
    }

    /**
     * Gets an image-data entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static ImageStore getEntry(int id) throws StoreException {
        return _SystemSupport.getImageEntry(id);
    }

    /**
     * Gets image-data entry IDs contained directly under the specified folder.
     *
     * @param folderId the folder entry ID
     * @return the entry-ID list, or {@code null}
     * @throws StoreException if the folder does not exist
     */
    public static int[] getEntryIds(int folderId) throws StoreException {
        return _SystemSupport.getImageEntryIds(folderId);
    }

    /**
     * Registers one image entry.
     *
     * @param image the image to register
     * @param exclusive whether the entry is exclusive to the current application
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(MediaImage image, boolean exclusive) throws InterruptedOperationException {
        return _SystemSupport.addImage(image, exclusive);
    }

    /**
     * Registers one image entry.
     *
     * @param image the image to register
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(MediaImage image) throws InterruptedOperationException {
        return _SystemSupport.addImage(image, !image.isRedistributable());
    }

    /**
     * Registers multiple image entries.
     *
     * @param images the images to register
     * @return the registered entry IDs, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int[] addEntry(MediaImage[] images) throws InterruptedOperationException {
        return _SystemSupport.addImages(images);
    }

    /**
     * Gets the entry ID.
     *
     * @return the entry ID
     */
    public final int getId() {
        return id;
    }

    /**
     * Gets the image as a media image.
     *
     * @return the media image
     */
    public final MediaImage getImage() {
        return image;
    }

    /**
     * Gets an input stream for the file-image representation.
     *
     * @return a new input stream for the encoded image bytes
     */
    public final InputStream getInputStream() {
        return new ByteArrayInputStream(bytes.clone());
    }
}
