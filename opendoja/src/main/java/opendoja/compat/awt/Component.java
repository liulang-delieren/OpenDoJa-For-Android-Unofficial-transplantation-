package opendoja.compat.awt;

public class Component implements opendoja.compat.awt.image.ImageObserver,opendoja.compat.awt.MenuContainer,java.io.Serializable {
    public Component() { }
    public void repaint()  {  }
    public boolean requestFocusInWindow()  { return false; }
    public void setSize(opendoja.compat.awt.Dimension p0)  {  }
    public void setPreferredSize(opendoja.compat.awt.Dimension p0)  {  }
    public void setMinimumSize(opendoja.compat.awt.Dimension p0)  {  }
    public void setMaximumSize(opendoja.compat.awt.Dimension p0)  {  }
    public opendoja.compat.awt.Container getParent()  { return null; }
    public int getWidth()  { return 0; }
    public int getHeight()  { return 0; }
    public opendoja.compat.awt.Dimension getSize()  { return null; }
    public void setFocusable(boolean p0)  {  }
    public void addFocusListener(opendoja.compat.awt.event.FocusListener p0)  {  }
    public void addKeyListener(opendoja.compat.awt.event.KeyListener p0)  {  }
    public void addMouseListener(opendoja.compat.awt.event.MouseListener p0)  {  }
    public void addMouseMotionListener(opendoja.compat.awt.event.MouseMotionListener p0)  {  }
    public boolean isShowing()  { return false; }
    public static final float TOP_ALIGNMENT = 0.0f;
    public static final float CENTER_ALIGNMENT = 0.5f;
    public static final float BOTTOM_ALIGNMENT = 1.0f;
    public static final float LEFT_ALIGNMENT = 0.0f;
    public static final float RIGHT_ALIGNMENT = 1.0f;
}

