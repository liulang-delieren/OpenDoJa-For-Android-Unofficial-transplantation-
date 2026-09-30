package com.nttdocomo.star.system;

import com.nttdocomo.star.StarApplication;

/**
 * Star compatibility wrapper for static launcher requests.
 */
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
