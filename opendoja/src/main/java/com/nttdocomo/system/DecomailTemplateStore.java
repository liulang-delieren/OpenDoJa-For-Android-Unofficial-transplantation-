package com.nttdocomo.system;

/**
 * Represents one native Decomail-template entry.
 */
public final class DecomailTemplateStore {
    private final int id;
    private final String template;

    DecomailTemplateStore(int id, String template) {
        this.id = id;
        this.template = template == null ? "" : template;
    }

    /**
     * Registers a Decomail template through native-style user interaction.
     *
     * @param templateData the template string
     * @return the registered entry ID, or {@code -1}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(String templateData) throws InterruptedOperationException {
        return _SystemSupport.addDecomailTemplate(templateData);
    }

    /**
     * Obtains a Decomail-template entry through native-style user selection.
     *
     * @return the selected entry, or {@code null}
     * @throws InterruptedOperationException declared by the API contract
     */
    public static DecomailTemplateStore selectEntry() throws InterruptedOperationException {
        return _SystemSupport.selectDecomailTemplate();
    }

    /**
     * Gets a Decomail-template entry by entry ID.
     *
     * @param id the entry ID
     * @return the requested entry
     * @throws StoreException if the entry does not exist
     */
    public static DecomailTemplateStore getEntry(int id) throws StoreException {
        return _SystemSupport.getDecomailTemplate(id);
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
     * Gets the Decomail template.
     *
     * @return the template as a {@link StringBuffer}
     */
    public StringBuffer getDecomailTemplate() {
        return new StringBuffer(template);
    }
}
