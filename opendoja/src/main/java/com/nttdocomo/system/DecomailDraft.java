package com.nttdocomo.system;

import com.nttdocomo.lang.XString;

/**
 * Defines a Decomail draft.
 */
public final class DecomailDraft extends MailDraft {
    /**
     * Creates a Decomail draft.
     */
    public DecomailDraft() {
        this(null, (String[]) null, null);
    }

    /**
     * Creates a Decomail draft.
     *
     * @param subject the subject
     * @param addresses the recipient addresses
     * @param body the body
     */
    public DecomailDraft(String subject, String[] addresses, String body) {
        super(subject, addresses, body);
    }

    /**
     * Creates a Decomail draft.
     *
     * @param subject the subject
     * @param address the recipient address
     * @param body the body
     */
    public DecomailDraft(String subject, XString address, String body) {
        super(subject, address, body);
    }

    /**
     * Sets the Decomail body.
     *
     * @param body the body, or {@code null} to clear it
     */
    @Override
    public void setBody(String body) {
        _SystemSupport.validateMailText(body, true);
        this.body = body;
    }
}
