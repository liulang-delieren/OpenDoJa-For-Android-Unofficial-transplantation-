package opendoja.compat.awt.event;

public class MouseEvent extends opendoja.compat.awt.event.InputEvent {
    public MouseEvent() { }
    public int getClickCount()  { return 0; }
    public opendoja.compat.awt.Point getPoint()  { return null; }
    public void consume()  {  }
    public boolean isPopupTrigger()  { return false; }
    public opendoja.compat.awt.Component getComponent()  { return null; }
    public int getX()  { return 0; }
    public int getY()  { return 0; }
    public static final int MOUSE_FIRST = 500;
    public static final int MOUSE_LAST = 507;
    public static final int MOUSE_CLICKED = 500;
    public static final int MOUSE_PRESSED = 501;
    public static final int MOUSE_RELEASED = 502;
    public static final int MOUSE_MOVED = 503;
    public static final int MOUSE_ENTERED = 504;
    public static final int MOUSE_EXITED = 505;
    public static final int MOUSE_DRAGGED = 506;
    public static final int MOUSE_WHEEL = 507;
    public static final int NOBUTTON = 0;
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;
}

