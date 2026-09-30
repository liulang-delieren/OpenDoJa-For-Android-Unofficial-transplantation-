package com.nttdocomo.system;

import com.nttdocomo.lang.XString;

/**
 * Provides access to native mail acquisition and sending.
 */
public final class MailAgent {
    private MailAgent() {
    }

    /**
     * Gets the latest incoming mail when it is unread.
     *
     * @return the latest unread incoming mail, or {@code null}
     */
    public static Mail getLastIncoming() {
        return _SystemSupport.getLastIncomingMail();
    }

    /**
     * Gets the remaining bytes that can be set in a text-mail body.
     *
     * @param mail the mail draft
     * @return the remaining body bytes
     */
    public static int getRemainingBytes(MailDraft mail) {
        if (mail instanceof DecomailDraft decomail) {
            return getRemainingBytes(decomail);
        }
        if (mail == null) {
            throw new NullPointerException("mail");
        }
        if (mail.getClass() != MailDraft.class) {
            throw new IllegalArgumentException("Unsupported MailDraft subclass: " + mail.getClass().getName());
        }
        return _SystemSupport.mailRemainingBytes(mail);
    }

    /**
     * Sends text mail.
     *
     * @param subject the subject
     * @param addresses the recipient addresses
     * @param body the body
     * @return {@code true} if sending succeeds, otherwise {@code false}
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if native storage is full
     */
    public static boolean send(String subject, String[] addresses, String body)
            throws MailException, InterruptedOperationException, StoreException {
        MailDraft mail = new MailDraft(subject, addresses, body);
        return _SystemSupport.sendMail(mail);
    }

    /**
     * Sends text mail.
     *
     * @param subject the subject
     * @param address the recipient address
     * @param body the body
     * @return {@code true} if sending succeeds, otherwise {@code false}
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if native storage is full
     */
    public static boolean send(String subject, XString address, String body)
            throws MailException, InterruptedOperationException, StoreException {
        MailDraft mail = new MailDraft(subject, address, body);
        return _SystemSupport.sendMail(mail);
    }

    /**
     * Sends text mail described by the specified draft.
     *
     * @param mail the mail draft
     * @return {@code true} if sending succeeds, otherwise {@code false}
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if native storage is full
     */
    public static boolean send(MailDraft mail)
            throws MailException, InterruptedOperationException, StoreException {
        return _SystemSupport.sendMail(mail);
    }

    /**
     * Gets the remaining bytes that can be set in a Decomail body.
     *
     * @param mail the Decomail draft
     * @return the remaining bytes for the HTML and text parts
     */
    public static int getRemainingBytes(DecomailDraft mail) {
        return _SystemSupport.decomailRemainingBytes(mail);
    }

    /**
     * Sends Decomail.
     *
     * @param mail the Decomail draft
     * @return {@code true} if sending succeeds, otherwise {@code false}
     * @throws MailException declared by the API contract
     * @throws InterruptedOperationException declared by the API contract
     * @throws StoreException if native storage is full
     */
    public static boolean send(DecomailDraft mail)
            throws MailException, InterruptedOperationException, StoreException {
        return _SystemSupport.sendDecomail(mail);
    }
}
