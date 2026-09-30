package opendoja.compat.management;

public final class ManagementFactory {
    private ManagementFactory() {
    }

    public static RuntimeMXBean getRuntimeMXBean() {
        return new RuntimeMXBean();
    }
}