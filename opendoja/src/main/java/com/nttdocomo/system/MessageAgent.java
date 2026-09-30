package com.nttdocomo.system;

import com.nttdocomo.lang.XString;

/**
 * Provides access to message-i-appli message folders and sending.
 */
public final class MessageAgent implements MailConstants {
    private MessageAgent() {
    }

    /**
     * Gets the number of messages contained in the specified folder.
     *
     * @param type the folder type
     * @param unseen whether only unseen received messages are counted
     * @return the number of messages
     */
    public static int size(int type, boolean unseen) {
        return _SystemSupport.messageCount(type, unseen);
    }

    /**
     * Gets the message IDs contained in the specified folder.
     *
     * @param type the folder type
     * @param unseen whether only unseen received messages are returned
     * @return the message IDs in newest-first order
     */
    public static int[] getIds(int type, boolean unseen) {
        return _SystemSupport.messageIds(type, unseen);
    }

    /**
     * Gets a message.
     *
     * @param type the folder type
     * @param id the message ID
     * @return the requested message
     */
    public static Message getMessage(int type, int id) {
        return _SystemSupport.getMessage(type, id);
    }

    /**
     * Gets the remaining bytes available for the specified draft.
     *
     * @param message the draft
     * @return the remaining bytes
     */
    public static int getRemainingBytes(MessageDraft message) {
        return _SystemSupport.messageRemainingBytes(message);
    }

    /**
     * Sends a message.
     *
     * @param subject the subject
     * @param addresses the recipient addresses
     * @param body the body
     * @param data the message-specific binary data
     * @return {@code true} if sending succeeds
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if folder storage is full
     */
    public static boolean send(String subject, String[] addresses, String body, byte[] data)
            throws MailException, InterruptedOperationException, StoreException {
        return _SystemSupport.sendMessage(new MessageDraft(subject, addresses, body, data));
    }

    /**
     * Sends a message.
     *
     * @param subject the subject
     * @param address the recipient address
     * @param body the body
     * @param data the message-specific binary data
     * @return {@code true} if sending succeeds
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if folder storage is full
     */
    public static boolean send(String subject, XString address, String body, byte[] data)
            throws MailException, InterruptedOperationException, StoreException {
        return _SystemSupport.sendMessage(new MessageDraft(subject, address, body, data));
    }

    /**
     * Sends a draft.
     *
     * @param message the draft
     * @return {@code true} if sending succeeds
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if folder storage is full
     */
    public static boolean send(MessageDraft message)
            throws MailException, InterruptedOperationException, StoreException {
        return _SystemSupport.sendMessage(message);
    }

    /**
     * Re-sends a sent or unsent message.
     *
     * @param message the message to re-send
     * @return {@code true} if sending succeeds
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if folder storage is full
     */
    public static boolean send(MessageSent message)
            throws MailException, InterruptedOperationException, StoreException {
        return _SystemSupport.resendMessage(message);
    }

    /**
     * Deletes a message from the specified folder.
     *
     * @param type the folder type
     * @param id the message ID
     */
    public static void delete(int type, int id) {
        _SystemSupport.deleteMessage(type, id);
    }

    /**
     * Sets the seen state of a received message.
     *
     * @param id the received-message ID
     * @param seen whether the message should be marked seen
     */
    public static void setSeen(int id, boolean seen) {
        _SystemSupport.setMessageSeen(id, seen);
    }

    /**
     * Gets whether the specified received message is seen.
     *
     * @param id the received-message ID
     * @return {@code true} if the message is seen
     */
    public static boolean isSeen(int id) {
        return _SystemSupport.isMessageSeen(id);
    }

    /**
     * Registers a message-folder listener.
     *
     * @param listener the listener, or {@code null} to unregister it
     */
    public static void setMessageFolderListener(MessageFolderListener listener) {
        _SystemSupport.setMessageFolderListener(listener);
    }

    /**
     * Dispatches a message-folder change event.
     *
     * @param type the folder type
     * @param param1 ignored on the desktop host
     * @param param2 ignored on the desktop host
     */
    public static void dispatchEvent(int type, int param1, int param2) {
        _SystemSupport.dispatchMessageFolderEvent(type, param1, param2);
    }
}
