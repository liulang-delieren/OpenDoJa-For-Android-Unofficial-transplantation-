package opendoja.probes;

import com.nttdocomo.ui.*;
import opendoja.host.DesktopLauncher;

public final class ConstructorCurrentFrameProbe {
    private ConstructorCurrentFrameProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        IApplication app = DesktopLauncher.launch(ConstructorCurrentApp.class);
        if (!(Display.getCurrent() instanceof ConstructorCanvas)) {
            throw new IllegalStateException("Constructor-time Display.setCurrent() was not preserved");
        }
        app.terminate();
        DemoLog.info(ConstructorCurrentFrameProbe.class, "Constructor current-frame probe OK");
    }

    public static final class ConstructorCurrentApp extends IApplication {
        public ConstructorCurrentApp() {
            Display.setCurrent((Frame) new ConstructorCanvas());
        }

        @Override
        public void start() {
        }
    }

    static final class ConstructorCanvas extends Canvas {
        @Override
        public void paint(Graphics g) {
            g.lock();
            g.clearRect(0, 0, Display.getWidth(), Display.getHeight());
            g.drawString("ctor", 0, 0);
            g.unlock(true);
        }
    }
}
