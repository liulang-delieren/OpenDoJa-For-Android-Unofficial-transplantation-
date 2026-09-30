package com.nttdocomo.system;

import com.nttdocomo.util.EventListener;

/**
 * Defines a listener for changes to message folders.
 */
public interface MessageFolderListener extends EventListener, MailConstants {
    /**
     * Called when a folder has changed.
     *
     * @param type the changed folder type
     */
    void folderChanged(int type);
}
