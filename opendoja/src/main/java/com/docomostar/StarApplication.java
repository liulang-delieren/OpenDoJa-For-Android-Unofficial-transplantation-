package com.docomostar;

import com.nttdocomo.ui.IApplication;

public abstract class StarApplication extends IApplication {
    public static final int APP_STATE_ACTIVE = 1;
    public static final int APP_STATE_SUSPENDED = 2;

    private final StarApplicationManager manager = new StarApplicationManager(this);

    public static StarApplication getThisStarApplication() {
        IApplication app = IApplication.getCurrentApp();
        return app instanceof StarApplication starApplication ? starApplication : null;
    }

    public final StarApplicationManager getStarApplicationManager() {
        return manager;
    }

    @Override
    public void start() {
        started(getLaunchType());
        activated(getAppState());
    }

    @Override
    public void resume() {
        activated(getAppState());
    }

    public void started(int launchType) {
        started();
    }

    public void started() {
        return;
    }

    public void activated() {
        return;
    }

    public void activated(int appState) {
        activated();
    }

    public void stateChanged(StarEventObject event) {
        stateChanged();
    }

    public void stateChanged() {
        return;
    }

    public void suspend() {
        return;
    }

    public int getAppState() {
        return APP_STATE_ACTIVE;
    }
}
