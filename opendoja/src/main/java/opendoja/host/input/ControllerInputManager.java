package opendoja.host.input;

public final class ControllerInputManager implements AutoCloseable {
    private volatile boolean closed;

    public ControllerInputManager(Object ownerWindow) {
    }

    public void addListener(ControllerInputListener listener) {
    }

    @Override
    public void close() {
        closed = true;
    }
}
