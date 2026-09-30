package opendoja.compat.swing;

public class TransferHandler {
    public TransferHandler() { }
    public static final int NONE = 0;
    public static final int COPY = 1;
    public static final int MOVE = 2;
    public static final int COPY_OR_MOVE = 3;
    public static final int LINK = 1073741824;
    public interface HasGetTransferHandler {
    }

}

