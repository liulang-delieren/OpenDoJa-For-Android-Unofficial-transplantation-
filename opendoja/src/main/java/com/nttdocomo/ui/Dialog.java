package com.nttdocomo.ui;

import com.nttdocomo.lang.UnsupportedOperationException;
import opendoja.host.DoJaRuntime;

import opendoja.compat.swing.*;

/**
 * Defines a dialog.
 * The dialog class provides a function in the low-level API and high-level API
 * for presenting a message to the user and asking for confirmation.
 * A dialog has buttons, and the displayed buttons depend on the dialog type.
 *
 * <p>While a dialog is displayed, if another thread tries to switch the canvas
 * or panel behind it, the switch occurs after the dialog is closed.
 * If a dialog is specified as an argument to
 * {@code Display.setCurrent(Frame)}, an exception occurs.</p>
 *
 * @see Canvas
 * @see Panel
 */
public final class Dialog extends Frame {
    /**
     * A dialog type that represents an information dialog ({@code =0}).
     * It has an OK button.
     */
    public static final int DIALOG_INFO = 0;
    /**
     * A dialog type that represents a warning dialog ({@code =1}).
     * It has an OK button.
     */
    public static final int DIALOG_WARNING = 1;
    /**
     * A dialog type that represents an error dialog ({@code =2}).
     * It has an OK button.
     */
    public static final int DIALOG_ERROR = 2;
    /**
     * A dialog type that represents a dialog with YES and NO buttons
     * ({@code =3}).
     */
    public static final int DIALOG_YESNO = 3;
    /**
     * A dialog type that represents a dialog with YES, NO, and Cancel buttons
     * ({@code =4}).
     */
    public static final int DIALOG_YESNOCANCEL = 4;
    /**
     * Represents the OK button ({@code =0x0001}).
     */
    public static final int BUTTON_OK = 1;
    /**
     * Represents the Cancel button ({@code =0x0002}).
     */
    public static final int BUTTON_CANCEL = 2;
    /**
     * Represents the YES button ({@code =0x0004}).
     */
    public static final int BUTTON_YES = 4;
    /**
     * Represents the NO button ({@code =0x0008}).
     */
    public static final int BUTTON_NO = 8;

    private final int type;
    private String title;
    private String text = "";
    private Font font = Font.getDefaultFont();
    private boolean showing;

    /**
     * Creates a dialog by specifying the dialog type and the dialog-title
     * string.
     * If the title string is too long to fit in the dialog, the part that
     * cannot be displayed is clipped.
     *
     * @param type the dialog type
     * @param title the dialog-title string; if {@code null}, the empty string
     *              ({@code ""}) is set
     * @throws IllegalArgumentException if {@code type} is invalid
     */
    public Dialog(int type, String title) {
        this.type = validateType(type);
        this.title = title == null ? "" : title;
    }

    /**
     * Sets the background color.
     * The background color is changed immediately when this method is called.
     * By default, a device-dependent color is set.
     *
     * @param color the integer value representing the background color
     */
    @Override
    public void setBackground(int color) {
        super.setBackground(color);
    }

    /**
     * Sets the font used to draw strings in this dialog.
     * It does not affect the title string or the button strings.
     * By default, the font returned by {@link Font#getDefaultFont()} when the
     * dialog is created is set.
     *
     * @param font the font used for drawing
     * @throws NullPointerException if {@code font} is {@code null}
     */
    public void setFont(Font font) {
        if (font == null) {
            throw new NullPointerException("font");
        }
        this.font = font;
    }

    /**
     * Sets the message string to display.
     * By default, the empty string ({@code ""}) is set.
     * If the message string is too long to fit in the dialog, the part that
     * cannot be displayed is clipped.
     *
     * @param text the message string
     */
    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    /**
     * Displays the dialog on the screen.
     *
     * @return the button that was pressed
     * @throws UIException if the dialog is displayed while it is already being
     *         shown ({@link UIException#BUSY_RESOURCE})
     */
    public int show() {
        if (showing) {
            throw new UIException(UIException.BUSY_RESOURCE, "Dialog is already showing");
        }
        showing = true;
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            runtime.beginModalDialog();
        }
        try {
        if (type == DIALOG_YESNO) {
            int result = JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_OPTION);
            return result == JOptionPane.YES_OPTION ? BUTTON_YES : BUTTON_NO;
        }
        if (type == DIALOG_YESNOCANCEL) {
            int result = JOptionPane.showConfirmDialog(null, text, title, JOptionPane.YES_NO_CANCEL_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                return BUTTON_YES;
            }
            if (result == JOptionPane.NO_OPTION) {
                return BUTTON_NO;
            }
            return BUTTON_CANCEL;
        }
        JOptionPane.showMessageDialog(null, text, title, switch (type) {
            case DIALOG_WARNING -> JOptionPane.WARNING_MESSAGE;
            case DIALOG_ERROR -> JOptionPane.ERROR_MESSAGE;
            default -> JOptionPane.INFORMATION_MESSAGE;
        });
        return BUTTON_OK;
        } finally {
            if (runtime != null) {
                runtime.endModalDialog();
            }
            showing = false;
        }
    }

    /**
     * Sets the soft-key label string.
     * In the {@code Dialog} class, the soft-key label string cannot be changed,
     * so this method is overridden to stop the function.
     * Therefore, calling this method does nothing.
     *
     * @param key the soft-key number whose label string is set
     * @param caption the label string
     */
    @Override
    public void setSoftLabel(int key, String caption) {
        int ignoredKey = key;
        String ignoredCaption = caption;
    }

    /**
     * Sets whether the soft keys are displayed.
     * In the {@code Dialog} class, soft keys cannot be shown or hidden, so this
     * method always throws an exception.
     *
     * @param visible this value has no meaning
     * @throws UnsupportedOperationException whenever this method is called
     */
    @Override
    public void setSoftLabelVisible(boolean visible) {
        throw softLabelVisibilityFailure();
    }

    private static int validateType(int type) {
        if (type < DIALOG_INFO || type > DIALOG_YESNOCANCEL) {
            throw new IllegalArgumentException("type");
        }
        return type;
    }

    private static UnsupportedOperationException softLabelVisibilityFailure() {
        return new UnsupportedOperationException("Dialog soft-label visibility cannot be changed");
    }
}
