package com.docomostar.media;

import com.nttdocomo.lang.IterationAbortedException;
import com.nttdocomo.ui.AvatarData;
import com.nttdocomo.ui.ExifData;
import com.nttdocomo.ui.Image;
import com.nttdocomo.ui.MediaData;

import java.io.InputStream;

public final class MediaManager {
    private MediaManager() {
    }

    public static MediaData getData(String name) {
        return com.nttdocomo.ui.MediaManager.getData(name);
    }

    public static MediaData getData(InputStream inputStream) {
        return com.nttdocomo.ui.MediaManager.getData(inputStream);
    }

    public static MediaData getData(byte[] data) {
        return com.nttdocomo.ui.MediaManager.getData(data);
    }

    public static MediaImage getImage(String name) {
        return adaptImage(com.nttdocomo.ui.MediaManager.getImage(name));
    }

    public static MediaImage getImage(InputStream inputStream) {
        return adaptImage(com.nttdocomo.ui.MediaManager.getImage(inputStream));
    }

    public static MediaImage getImage(byte[] data) {
        return adaptImage(com.nttdocomo.ui.MediaManager.getImage(data));
    }

    public static MediaImage getStreamingImage(String location, String contentType) {
        return adaptImage(com.nttdocomo.ui.MediaManager.getStreamingImage(location, contentType));
    }

    public static MediaSound getSound(String name) {
        return adaptSound(com.nttdocomo.ui.MediaManager.getSound(name));
    }

    public static MediaSound getSound(InputStream inputStream) {
        return adaptSound(com.nttdocomo.ui.MediaManager.getSound(inputStream));
    }

    public static MediaSound getSound(byte[] data) {
        return adaptSound(com.nttdocomo.ui.MediaManager.getSound(data));
    }

    public static AvatarData getAvatarData(String name) {
        return com.nttdocomo.ui.MediaManager.getAvatarData(name);
    }

    public static AvatarData getAvatarData(InputStream inputStream) {
        return com.nttdocomo.ui.MediaManager.getAvatarData(inputStream);
    }

    public static AvatarData getAvatarData(byte[] data) {
        return com.nttdocomo.ui.MediaManager.getAvatarData(data);
    }

    public static void use(MediaImage[] mediaImages, boolean exclusive) throws IterationAbortedException {
        com.nttdocomo.ui.MediaManager.use(mediaImages, exclusive);
    }

    public static void use(MediaSound[] mediaSounds, boolean exclusive) throws IterationAbortedException {
        com.nttdocomo.ui.MediaManager.use(mediaSounds, exclusive);
    }

    public static MediaImage createMediaImage(int width, int height) {
        return adaptImage(com.nttdocomo.ui.MediaManager.createMediaImage(width, height));
    }

    public static MediaSound createMediaSound(int size) {
        return adaptSound(com.nttdocomo.ui.MediaManager.createMediaSound(size));
    }

    private static MediaImage adaptImage(com.nttdocomo.ui.MediaImage image) {
        if (image == null) {
            return null;
        }
        if (image instanceof MediaImage mediaImage) {
            return mediaImage;
        }
        return new MediaImageAdapter(image);
    }

    private static MediaSound adaptSound(com.nttdocomo.ui.MediaSound sound) {
        if (sound == null) {
            return null;
        }
        if (sound instanceof MediaSound mediaSound) {
            return mediaSound;
        }
        return new MediaSoundAdapter(sound);
    }

    private static final class MediaImageAdapter implements MediaImage {
        private final com.nttdocomo.ui.MediaImage delegate;

        private MediaImageAdapter(com.nttdocomo.ui.MediaImage delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getWidth() {
            return delegate.getWidth();
        }

        @Override
        public int getHeight() {
            return delegate.getHeight();
        }

        @Override
        public Image getImage() {
            return delegate.getImage();
        }

        @Override
        public ExifData getExifData() {
            return delegate.getExifData();
        }

        @Override
        public void setExifData(ExifData exifData) {
            delegate.setExifData(exifData);
        }

        @Override
        public void use() throws com.nttdocomo.io.ConnectionException {
            delegate.use();
        }

        @Override
        public void use(com.nttdocomo.ui.MediaResource other, boolean exclusive) throws com.nttdocomo.io.ConnectionException {
            delegate.use(other, exclusive);
        }

        @Override
        public void unuse() {
            delegate.unuse();
        }

        @Override
        public void dispose() {
            delegate.dispose();
        }

        @Override
        public String getProperty(String key) {
            return delegate.getProperty(key);
        }

        @Override
        public void setProperty(String key, String value) {
            delegate.setProperty(key, value);
        }

        @Override
        public boolean isRedistributable() {
            return delegate.isRedistributable();
        }

        @Override
        public boolean setRedistributable(boolean redistributable) {
            return delegate.setRedistributable(redistributable);
        }
    }

    private static final class MediaSoundAdapter implements MediaSound {
        private final com.nttdocomo.ui.MediaSound delegate;

        private MediaSoundAdapter(com.nttdocomo.ui.MediaSound delegate) {
            this.delegate = delegate;
        }

        @Override
        public void use() throws com.nttdocomo.io.ConnectionException {
            delegate.use();
        }

        @Override
        public void use(com.nttdocomo.ui.MediaResource other, boolean exclusive) throws com.nttdocomo.io.ConnectionException {
            delegate.use(other, exclusive);
        }

        @Override
        public void unuse() {
            delegate.unuse();
        }

        @Override
        public void dispose() {
            delegate.dispose();
        }

        @Override
        public String getProperty(String key) {
            return delegate.getProperty(key);
        }

        @Override
        public void setProperty(String key, String value) {
            delegate.setProperty(key, value);
        }

        @Override
        public boolean isRedistributable() {
            return delegate.isRedistributable();
        }

        @Override
        public boolean setRedistributable(boolean redistributable) {
            return delegate.setRedistributable(redistributable);
        }
    }
}
