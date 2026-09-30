package opendoja.compat.awt;

public class RenderingHints implements java.util.Map<java.lang.Object, java.lang.Object>, java.lang.Cloneable {
    public static opendoja.compat.awt.RenderingHints.Key KEY_TEXT_ANTIALIASING = new Key();
    public static opendoja.compat.awt.RenderingHints.Key KEY_FRACTIONALMETRICS = new Key();
    public static java.lang.Object VALUE_FRACTIONALMETRICS_OFF = new Object();
    public static java.lang.Object VALUE_TEXT_ANTIALIAS_OFF = new Object();
    public static java.lang.Object VALUE_TEXT_ANTIALIAS_GASP = new Object();
    public static java.lang.Object VALUE_TEXT_ANTIALIAS_LCD_HRGB = new Object();
    public static java.lang.Object VALUE_TEXT_ANTIALIAS_ON = new Object();
    public static opendoja.compat.awt.RenderingHints.Key KEY_ANTIALIASING = new Key();
    public static java.lang.Object VALUE_ANTIALIAS_OFF = new Object();
    public static java.lang.Object VALUE_ANTIALIAS_ON = new Object();
    public static opendoja.compat.awt.RenderingHints.Key KEY_INTERPOLATION = new Key();
    public static java.lang.Object VALUE_INTERPOLATION_NEAREST_NEIGHBOR = new Object();
    public static java.lang.Object VALUE_INTERPOLATION_BILINEAR = new Object();
    public static java.lang.Object VALUE_INTERPOLATION_BICUBIC = new Object();

    private final java.util.HashMap<java.lang.Object, java.lang.Object> map = new java.util.HashMap<>();

    public RenderingHints() {
    }

    public RenderingHints(java.util.Map<? extends java.lang.Object, ? extends java.lang.Object> hints) {
        if (hints != null) {
            map.putAll(hints);
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(java.lang.Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(java.lang.Object value) {
        return map.containsValue(value);
    }

    public java.lang.Object get(java.lang.Object key) {
        return map.get(key);
    }

    public java.lang.Object put(java.lang.Object key, java.lang.Object value) {
        return map.put(key, value);
    }

    public java.lang.Object remove(java.lang.Object key) {
        return map.remove(key);
    }

    public void putAll(java.util.Map<? extends java.lang.Object, ? extends java.lang.Object> m) {
        if (m != null) {
            map.putAll(m);
        }
    }

    public void clear() {
        map.clear();
    }

    public java.util.Set<java.lang.Object> keySet() {
        return map.keySet();
    }

    public java.util.Collection<java.lang.Object> values() {
        return map.values();
    }

    public java.util.Set<java.util.Map.Entry<java.lang.Object, java.lang.Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object clone() {
        RenderingHints copy = new RenderingHints(map);
        return copy;
    }

    public static class Key {
        private static int nextId;

        private final int id;

        public Key() {
            this.id = nextId++;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
}
