package com.nttdocomo.star.system;

import com.nttdocomo.star.StarApplication;
import com.nttdocomo.ui.IApplication;

/**
 * Star compatibility bridge for invitation-based app launches.
 */
public final class Invitation {
    private Invitation() {
    }

    public static boolean sendLaunchRequest(InvitationParam param) {
        StarApplication application = StarApplication.getThisStarApplication();
        if (application == null || param == null) {
            return false;
        }
        String launchParameter = param.getLaunchParameter();
        String[] args = launchParameter == null ? new String[0] : new String[]{launchParameter};
        application.launch(IApplication.LAUNCH_IAPPLI, args);
        return true;
    }
}
