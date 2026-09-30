package com.nttdocomo.star.system;

import com.nttdocomo.lang.XString;
import com.nttdocomo.lang._XStringSupport;

/**
 * Star compatibility data object for launcher invitations.
 */
public final class InvitationParam {
    private final String launcherAppName;
    private final String launchParameter;
    private final int downloadBehavior;
    private final String expirationText;
    private final int expiration;

    public InvitationParam(String launcherAppName, XString launchParameter,
                           int downloadBehavior, String expirationText, int expiration) {
        this.launcherAppName = launcherAppName;
        this.launchParameter = _XStringSupport.valueOrNull(launchParameter);
        this.downloadBehavior = downloadBehavior;
        this.expirationText = expirationText;
        this.expiration = expiration;
    }

    public int getDownloadBehavior() {
        return downloadBehavior;
    }

    public int getExpiration() {
        return expiration;
    }

    public String getLauncherAppName() {
        return launcherAppName;
    }

    public String getLaunchParameter() {
        return launchParameter;
    }

    public String getExpirationText() {
        return expirationText;
    }
}
