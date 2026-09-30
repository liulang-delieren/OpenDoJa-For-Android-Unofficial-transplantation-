package opendoja.android;

import android.content.Context;

import java.io.File;

/** Shared helpers for the on-device games directory. */
final class JamAssets {

    private JamAssets() {
    }

    static File gamesDir(Context context) {
        File dir = new File(context.getFilesDir(), "games");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IllegalStateException("Cannot create games dir: " + dir);
        }
        return dir;
    }
}
