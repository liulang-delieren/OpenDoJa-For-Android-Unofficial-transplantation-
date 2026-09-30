package com.nttdocomo.system;

import com.nttdocomo.ui.AvatarData;

/**
 * Provides access to native avatar-data management.
 */
public final class AvatarStore {
    private AvatarStore() {
    }

    /**
     * Registers avatar data through native-style user interaction.
     *
     * @param avatar the avatar data to register
     * @return the registered entry ID, or {@code -1} if registration is cancelled
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(AvatarData avatar) throws InterruptedOperationException {
        return _SystemSupport.addAvatar(avatar);
    }

    /**
     * Obtains an avatar-data entry ID through native-style user selection.
     *
     * @return the selected entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int selectEntryId() throws InterruptedOperationException {
        return _SystemSupport.selectAvatarEntryId();
    }
}
