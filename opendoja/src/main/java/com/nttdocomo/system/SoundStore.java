package com.nttdocomo.system;

import com.nttdocomo.ui.MediaSound;

/**
 * Provides access to native sound-data registration.
 */
public final class SoundStore {
    private SoundStore() {
    }

    /**
     * Registers sound data through native-style user interaction.
     *
     * @param sound the sound-data media sound
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(MediaSound sound) throws InterruptedOperationException {
        return _SystemSupport.addSound(sound);
    }
}
