package opendoja.host;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.TextBox;
import com.nttdocomo.ui.UIException;

import opendoja.compat.swing.*;
import opendoja.compat.swing.text.AttributeSet;
import opendoja.compat.swing.text.BadLocationException;
import opendoja.compat.swing.text.PlainDocument;
import opendoja.compat.swing.text.JTextComponent;
import opendoja.compat.awt.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Host-side IME bridge for low-level DoJa canvases.
 */
public final class HostTextInput {
    private static final String AUTOMATION_CANCEL_TOKEN = "__CANCEL__";
    private static final AtomicReference<Canvas> ACTIVE_IME = new AtomicReference<>();

    private HostTextInput() {
    }

    public static void requestIme(Canvas canvas, String text, int displayMode, int inputMode, int inputSize) {
        Objects.requireNonNull(canvas, "canvas");
        if (!ACTIVE_IME.compareAndSet(null, canvas)) {
            throw new UIException(UIException.BUSY_RESOURCE, "IME already active");
        }
        String initialText = text == null ? "" : text;
        Runnable task = () -> showIme(canvas, text, initialText, displayMode, inputMode, inputSize);
        String automated = OpenDoJaLaunchArgs.get(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, null);
        if (automated != null || GraphicsEnvironment.isHeadless()) {
            Thread worker = new Thread(task, "openDoja-ime");
            worker.setDaemon(true);
            worker.start();
            return;
        }
        SwingUtilities.invokeLater(task);
    }

    private static void showIme(Canvas canvas, String originalText, String initialText,
                                int displayMode, int inputMode, int inputSize) {
        int eventType = Canvas.IME_CANCELED;
        String eventText = originalText;
        try {
            String automated = OpenDoJaLaunchArgs.get(OpenDoJaLaunchArgs.IME_AUTOMATION_RESPONSE, null);
            if (automated != null) {
                long delayMillis = OpenDoJaLaunchArgs.getLong(OpenDoJaLaunchArgs.IME_AUTOMATION_DELAY_MS, 0L);
                if (delayMillis > 0L) {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (!AUTOMATION_CANCEL_TOKEN.equals(automated)) {
                    eventType = Canvas.IME_COMMITTED;
                    eventText = clampToInputSize(automated, inputSize);
                }
                return;
            }
            if (GraphicsEnvironment.isHeadless()) {
                return;
            }

            JTextComponent field = createField(displayMode, initialText, inputSize);
            field.selectAll();
            int option = JOptionPane.showConfirmDialog(
                    dialogParent(),
                    field,
                    dialogTitle(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                eventType = Canvas.IME_COMMITTED;
                eventText = clampToInputSize(readFieldValue(field), inputSize);
            }
        } finally {
            ACTIVE_IME.compareAndSet(canvas, null);
            // DoJa imeOn() is asynchronous: callback delivery happens after the initiating call returns.
            canvas.processIMEEvent(eventType, eventText);
        }
    }

    private static JTextComponent createField(int displayMode, String initialText, int inputSize) {
        JTextComponent field = displayMode == TextBox.DISPLAY_PASSWORD
                ? new JPasswordField(initialText)
                : new JTextField(initialText);
        if (inputSize > 0) {
            PlainDocument document = new PlainDocument() {
                @Override
                public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                    if (str == null) {
                        return;
                    }
                    if (inputUnits(documentTextAfterInsert(this, offset, str)) > inputSize) {
                        return;
                    }
                    super.insertString(offset, str, attr);
                }

                @Override
                public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    if (inputUnits(documentTextAfterReplace(this, offset, length, text)) > inputSize) {
                        return;
                    }
                    super.replace(offset, length, text, attrs);
                }
            };
            field.setDocument(document);
            field.setText(initialText);
        }
        field.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        return field;
    }

    private static Component dialogParent() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null) {
            Component parent = runtime.dialogParent();
            if (parent != null) {
                return parent;
            }
        }
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }

    private static String dialogTitle() {
        DoJaRuntime runtime = DoJaRuntime.current();
        if (runtime != null && runtime.config() != null && runtime.config().title() != null
                && !runtime.config().title().isBlank()) {
            return runtime.config().title();
        }
        return "IME";
    }

    private static String readFieldValue(JTextComponent field) {
        if (field instanceof JPasswordField passwordField) {
            return new String(passwordField.getPassword());
        }
        return field.getText();
    }

    private static String clampToInputSize(String value, int inputSize) {
        if (value == null || inputSize <= 0 || inputUnits(value) <= inputSize) {
            return value;
        }
        StringBuilder clamped = new StringBuilder();
        int units = 0;
        int index = 0;
        while (index < value.length() && units < inputSize) {
            int codePoint = value.codePointAt(index);
            int width = isHalfWidth(codePoint) ? 1 : 2;
            if (units + width > inputSize) {
                break;
            }
            clamped.appendCodePoint(codePoint);
            units += width;
            index += Character.charCount(codePoint);
        }
        return clamped.toString();
    }

    public static int inputUnits(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        int units = 0;
        int index = 0;
        while (index < value.length()) {
            int codePoint = value.codePointAt(index);
            units += isHalfWidth(codePoint) ? 1 : 2;
            index += Character.charCount(codePoint);
        }
        return units;
    }

    private static boolean isHalfWidth(int codePoint) {
        return (codePoint >= 0x0000 && codePoint <= 0x00FF)
                || (codePoint >= 0xFF61 && codePoint <= 0xFF9F);
    }

    private static String documentTextAfterInsert(PlainDocument document, int offset, String inserted) throws BadLocationException {
        String current = document.getText(0, document.getLength());
        String normalized = inserted == null ? "" : inserted;
        return current.substring(0, offset) + normalized + current.substring(offset);
    }

    private static String documentTextAfterReplace(PlainDocument document, int offset, int length, String replacement)
            throws BadLocationException {
        String current = document.getText(0, document.getLength());
        String normalized = replacement == null ? "" : replacement;
        return current.substring(0, offset) + normalized + current.substring(offset + length);
    }
}
