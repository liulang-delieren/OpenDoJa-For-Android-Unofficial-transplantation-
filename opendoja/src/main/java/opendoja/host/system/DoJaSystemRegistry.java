package opendoja.host.system;

import com.nttdocomo.system.StoreException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal registry for host-visible DoJa system resources such as theme entries.
 */
public final class DoJaSystemRegistry {
    public enum EntryKind {
        IMAGE,
        MOVIE,
        SOUND,
        AVATAR
    }

    private static final Object LOCK = new Object();
    private static final Map<Integer, EntryKind> entryKinds = new HashMap<>();
    private static final Map<Integer, Integer> imageThemes = new HashMap<>();
    private static final Map<Integer, Integer> soundThemes = new HashMap<>();
    private static final Map<Integer, Integer> movieThemes = new HashMap<>();
    private static final Map<String, int[]> menuIcons = new HashMap<>();

    private DoJaSystemRegistry() {
    }

    public static void registerEntry(int id, EntryKind kind) {
        synchronized (LOCK) {
            entryKinds.put(id, kind);
        }
    }

    public static boolean hasEntry(int id, EntryKind kind) {
        synchronized (LOCK) {
            return entryKinds.get(id) == kind;
        }
    }

    public static void requireEntry(int id, EntryKind... allowedKinds) throws StoreException {
        synchronized (LOCK) {
            EntryKind actual = entryKinds.get(id);
            if (actual == null) {
                throw new StoreException(StoreException.NOT_FOUND, "Entry not found: " + id);
            }
            for (EntryKind allowedKind : allowedKinds) {
                if (actual == allowedKind) {
                    return;
                }
            }
            throw new StoreException(StoreException.NOT_FOUND,
                    "Entry kind " + actual + " not allowed here: " + Arrays.toString(allowedKinds));
        }
    }

    public static EntryKind entryKind(int id) {
        synchronized (LOCK) {
            return entryKinds.get(id);
        }
    }

    public static void setImageTheme(int target, int id) {
        synchronized (LOCK) {
            imageThemes.put(target, id);
            movieThemes.remove(target);
        }
    }

    public static void setSoundTheme(int target, int id) {
        synchronized (LOCK) {
            soundThemes.put(target, id);
            movieThemes.remove(target);
        }
    }

    public static void setMovieTheme(int target, int id) {
        synchronized (LOCK) {
            movieThemes.put(target, id);
            imageThemes.remove(target);
            soundThemes.remove(target);
        }
    }

    public static Integer getImageTheme(int target) {
        synchronized (LOCK) {
            return imageThemes.get(target);
        }
    }

    public static Integer getSoundTheme(int target) {
        synchronized (LOCK) {
            return soundThemes.get(target);
        }
    }

    public static Integer getMovieTheme(int target) {
        synchronized (LOCK) {
            return movieThemes.get(target);
        }
    }

    public static void setMenuIcons(int[] path, int[] ids) {
        synchronized (LOCK) {
            menuIcons.put(pathKey(path), ids.clone());
        }
    }

    public static int[] getMenuIcons(int[] path) {
        synchronized (LOCK) {
            int[] icons = menuIcons.get(pathKey(path));
            return icons == null ? null : icons.clone();
        }
    }

    private static String pathKey(int[] path) {
        if (path == null || path.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(path.length * 4);
        for (int value : path) {
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
