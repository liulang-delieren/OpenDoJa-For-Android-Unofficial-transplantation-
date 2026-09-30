package opendoja.compat.awt;

public class AWTEvent extends java.util.EventObject {
    public AWTEvent() { super(null); }
    public static final long COMPONENT_EVENT_MASK = 1l;
    public static final long CONTAINER_EVENT_MASK = 2l;
    public static final long FOCUS_EVENT_MASK = 4l;
    public static final long KEY_EVENT_MASK = 8l;
    public static final long MOUSE_EVENT_MASK = 16l;
    public static final long MOUSE_MOTION_EVENT_MASK = 32l;
    public static final long WINDOW_EVENT_MASK = 64l;
    public static final long ACTION_EVENT_MASK = 128l;
    public static final long ADJUSTMENT_EVENT_MASK = 256l;
    public static final long ITEM_EVENT_MASK = 512l;
    public static final long TEXT_EVENT_MASK = 1024l;
    public static final long INPUT_METHOD_EVENT_MASK = 2048l;
    public static final long PAINT_EVENT_MASK = 8192l;
    public static final long INVOCATION_EVENT_MASK = 16384l;
    public static final long HIERARCHY_EVENT_MASK = 32768l;
    public static final long HIERARCHY_BOUNDS_EVENT_MASK = 65536l;
    public static final long MOUSE_WHEEL_EVENT_MASK = 131072l;
    public static final long WINDOW_STATE_EVENT_MASK = 262144l;
    public static final long WINDOW_FOCUS_EVENT_MASK = 524288l;
    public static final int RESERVED_ID_MAX = 1999;
}

