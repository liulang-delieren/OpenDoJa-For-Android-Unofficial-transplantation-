package opendoja.compat.awt;

public class Desktop {
    public Desktop() { }
    public static boolean isDesktopSupported()  { return false; }
    public static opendoja.compat.awt.Desktop getDesktop()  { return null; }
    public boolean isSupported(opendoja.compat.awt.Desktop.Action p0)  { return false; }
    public void open(java.io.File p0)  throws java.io.IOException {  }
    public enum Action {
    ;
        public static opendoja.compat.awt.Desktop.Action OPEN = null;
    }

}

