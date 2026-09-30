package opendoja.host;

import opendoja.compat.awt.Dimension;
import opendoja.compat.awt.Point;

public final class HostScaleProbe {
    private HostScaleProbe() {
    }

    public static void main(String[] args) {
        check(HostScale.DEFAULT_ID.equals(HostScale.normalizeId(null)), "null host scale should normalize to default");
        check(HostScale.DEFAULT_ID.equals(HostScale.normalizeId("bogus")), "invalid host scale should normalize to default");
        check("4".equals(HostScale.normalizeId("999")), "fixed host scale should clamp high");
        check("1".equals(HostScale.normalizeId("-1")), "fixed host scale should clamp low");
        check(HostScale.FULLSCREEN_ID.equals(HostScale.normalizeId("fullscreen")),
                "fullscreen should normalize to fullscreen");
        check("Fullscreen".equals(HostScale.label(HostScale.FULLSCREEN_ID)),
                "fullscreen label should be concise");
        check(!DoJaRuntime.isWindowResizableForHostScale("1"),
                "fixed host scale should keep the host window non-resizable");
        check(DoJaRuntime.isWindowResizableForHostScale(HostScale.FULLSCREEN_ID),
                "fullscreen host scale should allow the host window to become resizable");

        ExternalFrameRenderer framedRenderer = new ExternalFrameRenderer(true,
                LaunchConfig.DEFAULT_STATUS_BAR_ICON_DEVICE,
                LaunchConfig.IAppliType.I_APPLI);
        ExternalFrameLayout framed = framedRenderer.layoutFor(240, 320, 2.5d);
        check(framed.preferredSize().equals(new Dimension(610, 895)),
                "framed fractional scale should preserve uniform proportions: " + framed.preferredSize());
        check(framed.drawArea().width == 240 && framed.drawArea().height == 320,
                "framed draw area should stay in unscaled game pixels");

        ExternalFrameRenderer unframedRenderer = new ExternalFrameRenderer(false,
                LaunchConfig.DEFAULT_STATUS_BAR_ICON_DEVICE,
                LaunchConfig.IAppliType.I_APPLI);
        ExternalFrameLayout unframed = unframedRenderer.layoutFor(240, 320, 2.5d);
        check(unframed.preferredSize().equals(new Dimension(600, 800)),
                "unframed fractional scale should preserve uniform proportions: " + unframed.preferredSize());

        double exactFit = DoJaRuntime.resolveFullscreenHostScale(
                new Dimension(244, 358),
                new Dimension(488, 716));
        check(close(exactFit, 2.0d), "fullscreen scale should choose exact fit but was " + exactFit);

        double heightLimited = DoJaRuntime.resolveFullscreenHostScale(
                new Dimension(244, 358),
                new Dimension(1000, 1000));
        check(close(heightLimited, 1000d / 358d), "fullscreen scale should use limiting dimension but was " + heightLimited);

        double belowNative = DoJaRuntime.resolveFullscreenHostScale(
                new Dimension(244, 358),
                new Dimension(122, 179));
        check(close(belowNative, 0.5d), "fullscreen scale should shrink below native size when needed but was " + belowNative);

        Point origin = DoJaRuntime.frameOriginForHostScale(
                HostScale.FULLSCREEN_ID,
                new Dimension(800, 600),
                new Dimension(600, 450));
        check(origin.equals(new Point(100, 75)), "fullscreen should center the scaled frame inside the outer black fill");

        Point fixedOrigin = DoJaRuntime.frameOriginForHostScale(
                "2",
                new Dimension(800, 600),
                new Dimension(600, 450));
        check(fixedOrigin.equals(new Point(0, 0)), "fixed scale should keep the frame at the panel origin");

        System.out.println("Host scale probe OK");
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.000001d;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
