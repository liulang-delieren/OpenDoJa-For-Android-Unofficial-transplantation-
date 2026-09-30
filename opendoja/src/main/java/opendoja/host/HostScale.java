package opendoja.host;

import java.util.Locale;

public final class HostScale {
    public static final int MIN_FIXED_SCALE = 1;
    public static final int MAX_FIXED_SCALE = 4;
    public static final String FULLSCREEN_ID = "fullscreen";
    public static final String DEFAULT_ID = "1";

    private HostScale() {
    }

    public static String normalizeId(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return DEFAULT_ID;
        }
        String normalized = candidate.trim().toLowerCase(Locale.ROOT);
        if (FULLSCREEN_ID.equals(normalized)) {
            return FULLSCREEN_ID;
        }
        try {
            int fixedScale = Integer.parseInt(normalized);
            return Integer.toString(normalizeFixedScale(fixedScale));
        } catch (NumberFormatException ignored) {
            return DEFAULT_ID;
        }
    }

    public static boolean isFullscreen(String candidate) {
        String normalized = normalizeId(candidate);
        return FULLSCREEN_ID.equals(normalized);
    }

    public static int fixedScaleOrDefault(String candidate, int defaultScale) {
        String normalized = normalizeId(candidate);
        if (FULLSCREEN_ID.equals(normalized)) {
            return normalizeFixedScale(defaultScale);
        }
        try {
            return normalizeFixedScale(Integer.parseInt(normalized));
        } catch (NumberFormatException ignored) {
            return normalizeFixedScale(defaultScale);
        }
    }

    public static int normalizeFixedScale(int scale) {
        return Math.max(MIN_FIXED_SCALE, Math.min(MAX_FIXED_SCALE, scale));
    }

    public static String label(String candidate) {
        String normalized = normalizeId(candidate);
        if (FULLSCREEN_ID.equals(normalized)) {
            return "Fullscreen";
        }
        int scale = fixedScaleOrDefault(normalized, MIN_FIXED_SCALE);
        return (scale * 100) + "%";
    }
}
