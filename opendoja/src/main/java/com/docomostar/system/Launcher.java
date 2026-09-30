package com.docomostar.system;

import com.docomostar.StarApplication;

public final class Launcher {
    private Launcher() {
    }

    public static void launch(int type, String[] args) {
        StarApplication application = StarApplication.getThisStarApplication();
        if (application != null) {
            application.launch(type, args);
        }
    }
}
