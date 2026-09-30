package opendoja.compat.swing;

public class JFrame extends opendoja.compat.awt.Frame implements opendoja.compat.swing.WindowConstants,opendoja.compat.swing.RootPaneContainer,opendoja.compat.swing.TransferHandler.HasGetTransferHandler {
    public JFrame() { }
    public boolean isDisplayable()  { return false; }
    public void validate()  {  }
    public void pack()  {  }
    public boolean isUndecorated()  { return false; }
    public void setResizable(boolean p0)  {  }
    public void setBounds(opendoja.compat.awt.Rectangle p0)  {  }
    public boolean isVisible()  { return false; }
    public void setVisible(boolean p0)  {  }
    public void toFront()  {  }
    public void removeWindowListener(opendoja.compat.awt.event.WindowListener p0)  {  }
    public void removeWindowFocusListener(opendoja.compat.awt.event.WindowFocusListener p0)  {  }
    public void dispose()  {  }
    public JFrame(java.lang.String p0) { }
    public void setUndecorated(boolean p0)  {  }
    public void setDefaultCloseOperation(int p0)  {  }
    public opendoja.compat.awt.Container getContentPane()  { return null; }
    public void setBackground(opendoja.compat.awt.Color p0)  {  }
    public void setAlwaysOnTop(boolean p0)  throws java.lang.SecurityException {  }
    public void addWindowListener(opendoja.compat.awt.event.WindowListener p0)  {  }
    public void addWindowFocusListener(opendoja.compat.awt.event.WindowFocusListener p0)  {  }
    public opendoja.compat.awt.Component add(opendoja.compat.awt.Component p0)  { return null; }
    public boolean isAlwaysOnTopSupported()  { return false; }
    public boolean isAlwaysOnTop()  { return false; }
    public boolean isFocused()  { return false; }
}

