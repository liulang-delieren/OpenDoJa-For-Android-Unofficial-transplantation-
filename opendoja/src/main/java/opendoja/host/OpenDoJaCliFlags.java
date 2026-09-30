package opendoja.host;

/**
 * Canonical registry for command-line flags accepted by openDoJa entry points.
 * Keep flag names and their meanings here so launcher/help text and argument parsing stay aligned.
 */
public final class OpenDoJaCliFlags {
    /** Overrides the simulated handset model for the launched application. */
    public static final String PHONE_MODEL = "--phone-model";
    /** Forces the i-appli launch mode, for example normal or standby startup. */
    public static final String LAUNCH_TYPE = "--launch-type";
    /** Rotates the presented display output: none, left, or right. */
    public static final String SCREEN_ROTATION = "--screen-rotation";
    /** Enables the host OpenGL ES FPS overlay for renderer diagnostics. */
    public static final String SHOW_OPEN_GLES_FPS = "--show-gles-fps";
    /** Runs the selected JAM in the current launcher process foreground path. */
    public static final String RUN_JAM = "--run-jam";
    /** Internal launcher handoff that invokes the dedicated JAM launcher entry point. */
    public static final String RUN_JAM_INTERNAL = "--run-jam-internal";
    /** Spawns the selected JAM in a separate background process and returns immediately. */
    public static final String SPAWN_JAM = "--spawn-jam";

    private OpenDoJaCliFlags() {
    }
}
