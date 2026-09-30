package com.nttdocomo.ui;

/**
 * Defines the interface that user-operable components implement.
 */
public interface Interactable {
    /**
     * Sets the component to the enabled state or the disabled state.
     * The component state can be changed regardless of whether the component is
     * visible or invisible.
     * The component is enabled when it is created.
     *
     * @param enabled {@code true} to enable the component, or {@code false} to
     *                disable it
     */
    void setEnabled(boolean enabled);

    /**
     * Requests that focus be set to the component.
     */
    void requestFocus();
}
