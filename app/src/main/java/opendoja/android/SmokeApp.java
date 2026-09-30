package opendoja.android;

import com.nttdocomo.ui.Canvas;
import com.nttdocomo.ui.Display;
import com.nttdocomo.ui.Graphics;
import com.nttdocomo.ui.IApplication;

public class SmokeApp extends IApplication {
    private volatile boolean running = true;
    private SmokeCanvas canvas;

    @Override
    public void start() {
        canvas = new SmokeCanvas();
        Display.setCurrent(canvas);
        canvas.repaint();
        Thread loop = new Thread(() -> {
            int frame = 0;
            while (running && !Thread.currentThread().isInterrupted()) {
                canvas.frame = frame++;
                canvas.repaint();
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "smoke-frames");
        loop.setDaemon(true);
        loop.start();
    }

    @Override
    public void resume() {
        running = true;
    }

    private static final class SmokeCanvas extends Canvas {
        volatile int frame;
        private int paintCount;

        @Override
        public void paint(Graphics g) {
            int w = Display.getWidth();
            int h = Display.getHeight();
            if (paintCount == 0 || paintCount % 120 == 0) {
                android.util.Log.i("OpenDoJa", "smoke paint#" + paintCount + " " + w + "x" + h
                        + " frame=" + frame);
            }
            paintCount++;

            g.setColor(0xFF102030);
            g.fillRect(0, 0, w, h);

            for (int y = 0; y < h; y++) {
                int r = (y * 255) / Math.max(1, h - 1);
                g.setColor(0xFF000000 | (r << 16) | 0x20);
                g.drawLine(0, y, w / 3, y);
            }

            int boxW = Math.max(16, w / 4);
            int boxH = Math.max(16, h / 6);
            int maxX = Math.max(1, w - boxW);
            int maxY = Math.max(1, h - boxH);
            int x = (frame * 5) % (maxX * 2);
            if (x > maxX) {
                x = (maxX * 2) - x;
            }
            int y = (frame * 3) % (maxY * 2);
            if (y > maxY) {
                y = (maxY * 2) - y;
            }
            g.setColor(0xFFFFCC00);
            g.fillRect(x, y, boxW, boxH);
            g.setColor(0xFFFFFFFF);
            g.drawRect(x, y, boxW, boxH);

            g.setColor(0xFF00FF88);
            g.drawLine(0, 0, w - 1, h - 1);
            g.setColor(0xFFFF4488);
            g.drawLine(w - 1, 0, 0, h - 1);

            g.setColor(0xFF000000);
            g.fillRect(0, h - 14, w, 14);
            g.setColor(0xFFFFFFFF);
            g.drawString("OpenDoJa smoke f=" + frame, 6, h - 3);
        }
    }
}
