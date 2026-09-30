package opendoja.compat.swing;

public class JOptionPane extends opendoja.compat.swing.JComponent {
    public JOptionPane() { }
    public static int showConfirmDialog(opendoja.compat.awt.Component p0, java.lang.Object p1, java.lang.String p2, int p3)  { return 0; }
    public static void showMessageDialog(opendoja.compat.awt.Component p0, java.lang.Object p1, java.lang.String p2, int p3)  {  }
    public static int showConfirmDialog(opendoja.compat.awt.Component p0, java.lang.Object p1, java.lang.String p2, int p3, int p4)  { return 0; }
    public static java.lang.Object showInputDialog(opendoja.compat.awt.Component p0, java.lang.Object p1, java.lang.String p2, int p3, opendoja.compat.swing.Icon p4, java.lang.Object[] p5, java.lang.Object p6)  { return null; }
    public static final int DEFAULT_OPTION = -1;
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 0;
    public static final int CLOSED_OPTION = -1;
    public static final int ERROR_MESSAGE = 0;
    public static final int INFORMATION_MESSAGE = 1;
    public static final int WARNING_MESSAGE = 2;
    public static final int QUESTION_MESSAGE = 3;
    public static final int PLAIN_MESSAGE = -1;
    public static final java.lang.String ICON_PROPERTY = "icon";
    public static final java.lang.String MESSAGE_PROPERTY = "message";
    public static final java.lang.String VALUE_PROPERTY = "value";
    public static final java.lang.String OPTIONS_PROPERTY = "options";
    public static final java.lang.String INITIAL_VALUE_PROPERTY = "initialValue";
    public static final java.lang.String MESSAGE_TYPE_PROPERTY = "messageType";
    public static final java.lang.String OPTION_TYPE_PROPERTY = "optionType";
    public static final java.lang.String SELECTION_VALUES_PROPERTY = "selectionValues";
    public static final java.lang.String INITIAL_SELECTION_VALUE_PROPERTY = "initialSelectionValue";
    public static final java.lang.String INPUT_VALUE_PROPERTY = "inputValue";
    public static final java.lang.String WANTS_INPUT_PROPERTY = "wantsInput";
}

