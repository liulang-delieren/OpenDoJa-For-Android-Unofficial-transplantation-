package opendoja.compat.awt;

public class KeyboardFocusManager implements opendoja.compat.awt.KeyEventDispatcher,opendoja.compat.awt.KeyEventPostProcessor {
    public KeyboardFocusManager() { }
    public static opendoja.compat.awt.KeyboardFocusManager getCurrentKeyboardFocusManager()  { return null; }
    public opendoja.compat.awt.Window getActiveWindow()  { return null; }
    public static final int FORWARD_TRAVERSAL_KEYS = 0;
    public static final int BACKWARD_TRAVERSAL_KEYS = 1;
    public static final int UP_CYCLE_TRAVERSAL_KEYS = 2;
    public static final int DOWN_CYCLE_TRAVERSAL_KEYS = 3;
}

