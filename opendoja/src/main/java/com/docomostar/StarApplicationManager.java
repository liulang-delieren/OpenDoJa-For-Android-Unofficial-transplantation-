package com.docomostar;

import com.nttdocomo.ui.IApplication;
import com.nttdocomo.ui.PushManager;
import com.nttdocomo.util.ScheduleDate;

public final class StarApplicationManager {
    private final StarApplication application;

    public StarApplicationManager(StarApplication application) {
        this.application = application;
    }

    public boolean isMoved() {
        return application.isMoved();
    }

    public void clearMoved() {
        application.clearMoved();
    }

    public boolean isMovedFromOtherTerminal() {
        return application.isMovedFromOtherTerminal();
    }

    public void setLaunchTime(int index, ScheduleDate date) {
        application.setLaunchTime(index, date);
    }

    public ScheduleDate getLaunchTime(int index) {
        return application.getLaunchTime(index);
    }

    public static String getSourceURL() {
        StarApplication application = StarApplication.getThisStarApplication();
        return application == null ? null : application.getSourceURL();
    }

    public static int getLaunchType() {
        StarApplication application = StarApplication.getThisStarApplication();
        return application == null ? IApplication.LAUNCHED_FROM_MENU : application.getLaunchType();
    }

    public static String getParameter(String key) {
        StarApplication application = StarApplication.getThisStarApplication();
        return application == null ? null : application.getParameter(key);
    }

    public static String[] getArgs() {
        StarApplication application = StarApplication.getThisStarApplication();
        return application == null ? new String[0] : application.getArgs();
    }

    public int getSuspendInfo() {
        return application.getSuspendInfo();
    }

    public PushManager getPushManager() throws SecurityException {
        return application.getPushManager();
    }

    public void launch(int type, String[] args) {
        application.launch(type, args);
    }

    public void terminate() {
        application.terminate();
    }

    public void upgrade() {
        return;
    }
}
