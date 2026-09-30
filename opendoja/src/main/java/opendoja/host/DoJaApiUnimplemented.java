package opendoja.host;

import com.nttdocomo.lang.UnsupportedOperationException;

/**
 * Shared helper for unresolved DoJa 5.1 APIs on the desktop host.
 */
public final class DoJaApiUnimplemented {
    private DoJaApiUnimplemented() {
    }

    public static UnsupportedOperationException unsupported(String signature, String reason) {
        log(signature, reason);
        return new UnsupportedOperationException("DoJa 5.1 API not implemented: " + signature);
    }

    public static void noOp(String signature, String reason) {
        log(signature, reason);
    }

    private static void log(String signature, String reason) {
        StringBuilder message = new StringBuilder("DoJa 5.1 API not implemented: ").append(signature);
        if (reason != null && !reason.isBlank()) {
            message.append(" - ").append(reason);
        }
        OpenDoJaLog.warn(DoJaApiUnimplemented.class, message.toString());
    }
}
