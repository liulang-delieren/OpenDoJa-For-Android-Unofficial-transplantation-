package com.nttdocomo.star;

import com.nttdocomo.ui.IApplication;

/**
 * Star-era application base class.
 * openDoJa maps Star titles onto the existing DoJa runtime and routes the
 * Star lifecycle callbacks through this compatibility layer.
 */
public abstract class StarApplication extends IApplication {
    public static final int APP_STATE_ACTIVE = 1;
    public static final int APP_STATE_SUSPENDED = 2;
    private final StarApplicationManager manager = new StarApplicationManager(this);

    /**
     * Gets the current Star application instance.
     *
     * @return the running Star application, or {@code null}
     */
    public static StarApplication getThisStarApplication() {
        IApplication app = IApplication.getCurrentApp();
        return app instanceof StarApplication starApplication ? starApplication : null;
    }

    /**
     * Gets the Star application manager for this application.
     *
     * @return the application manager
     */
    public final StarApplicationManager getStarApplicationManager() {
        return manager;
    }

    /**
     * Adapts the Star startup lifecycle to the DoJa host entry point.
     */
    @Override
    public void start() {
        started(getLaunchType());
        activated(getAppState());
    }

    /**
     * Star applications receive the activation callback after resuming too.
     */
    @Override
    public void resume() {
        activated(getAppState());
    }

    /**
     * Called once immediately after the application starts.
     *
     * @param launchType the Star launch reason
     */
    public void started(int launchType) {
        started();
    }

    /**
     * Compatibility hook for titles that implement the older no-arg pattern.
     */
    public void started() {
        return;
    }

    /**
     * Called when the application enters the active state.
     */
    public void activated() {
        return;
    }

    /**
     * Star titles often implement the int-accepting activation callback.
     *
     * @param appState the current Star application state
     */
    public void activated(int appState) {
        activated();
    }

    /**
     * Called when Star state changes are delivered to the application.
     *
     * @param event the state-change event
     */
    public void stateChanged(StarEventObject event) {
        stateChanged();
    }

    /**
     * Compatibility hook for titles that do not inspect the event payload.
     */
    public void stateChanged() {
        return;
    }

    /**
     * Star titles can request suspension. openDoJa currently treats this as a
     * compatibility no-op until the host models Star suspended-state behavior.
     */
    public void suspend() {
        return;
    }

    /**
     * Returns the coarse Star application state.
     *
     * @return the current application state
     */
    public int getAppState() {
        return APP_STATE_ACTIVE;
    }
}
