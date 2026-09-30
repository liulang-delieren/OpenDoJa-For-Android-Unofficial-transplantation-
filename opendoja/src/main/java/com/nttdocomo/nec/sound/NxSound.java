package com.nttdocomo.nec.sound;

import com.nttdocomo.io.ConnectionException;
import com.nttdocomo.ui.MediaListener;
import com.nttdocomo.ui.MediaManager;
import com.nttdocomo.ui.MediaPresenter;
import com.nttdocomo.ui.MediaResource;
import com.nttdocomo.ui.MediaSound;

/**
 * Minimal NEC sound wrapper reconstructed from the phone dump.
 *
 * Binary-backed evidence for this class boundary:
 * - the device dump contains the class name
 *   {@code com/nttdocomo/nec/sound/NxSound};
 * - the same dump contains {@code NxSound.java} next to a concrete
 *   {@code mediaAction(Lcom/nttdocomo/ui/MediaPresenter;II)V} descriptor;
 * - no additional public constructor, constant ownership, or other public
 *   method surface is proven by the recovered symbols.
 *
 * The runtime therefore exposes only the proven type plus the proven callback
 * signature, while delegating the standard {@link MediaSound} contract to an
 * internal empty sound resource until stronger device-backed evidence exists.
 */
public class NxSound implements MediaSound, MediaListener {
    private final MediaSound delegate;

    protected NxSound() {
        this.delegate = MediaManager.createMediaSound(0);
    }

    @Override
    public void use() throws ConnectionException {
        delegate.use();
    }

    @Override
    public void use(MediaResource overwritten, boolean useOnce) throws ConnectionException {
        delegate.use(overwritten, useOnce);
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

    @Override
    public void mediaAction(MediaPresenter presenter, int type, int param) {
    }
}
