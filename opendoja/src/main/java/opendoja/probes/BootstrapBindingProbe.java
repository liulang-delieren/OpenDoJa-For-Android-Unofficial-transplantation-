package opendoja.probes;

import com.nttdocomo.ui.IApplication;
import opendoja.host.DesktopLauncher;
import opendoja.host.DoJaRuntime;
import opendoja.host.LaunchConfig;

public final class BootstrapBindingProbe {
    private BootstrapBindingProbe() {
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");
        IApplication app = DesktopLauncher.launch(LaunchConfig.builder(BindingApp.class)
                .parameter("ProbeKey", "ProbeValue")
                .build());
        app.terminate();
        DemoLog.info(BootstrapBindingProbe.class, "Bootstrap binding probe OK");
    }

    public static final class BindingApp extends IApplication {
        public BindingApp() {
            DoJaRuntime runtime = DoJaRuntime.current();
            if (runtime == null) {
                throw new IllegalStateException("Runtime was not bootstrapped before app construction");
            }
            if (runtime.application() != this) {
                throw new IllegalStateException("Runtime application was not bound during app construction");
            }
            if (IApplication.getCurrentApp() != this) {
                throw new IllegalStateException("Current app was not set during app construction");
            }
            if (!"ProbeValue".equals(getParameter("ProbeKey"))) {
                throw new IllegalStateException("Prepared launch parameters were not visible in app construction");
            }
        }

        @Override
        public void start() {
        }
    }
}
