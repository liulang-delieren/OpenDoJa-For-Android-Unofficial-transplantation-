package com.nttdocomo.system;

/**
 * Provides access to native bookmark registration.
 */
public final class Bookmark {
    private Bookmark() {
    }

    /**
     * Registers a bookmark entry through native-style user interaction.
     *
     * @param url the bookmark URL
     * @param title the bookmark title
     * @return a non-{@code -1} invalid entry ID if registration succeeds
     * @throws InterruptedOperationException declared by the API contract
     */
    public static int addEntry(String url, String title) throws InterruptedOperationException {
        return _SystemSupport.addBookmark(url, title);
    }
}
