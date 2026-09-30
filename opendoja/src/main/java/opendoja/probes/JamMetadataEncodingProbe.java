package opendoja.probes;

import com.nttdocomo.ui.IApplication;
import opendoja.host.DoJaEncoding;
import opendoja.host.JamLauncher;
import opendoja.host.LaunchConfig;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public final class JamMetadataEncodingProbe {
    private static final Charset CP932 = Charset.forName("MS932");
    private static final String EXPECTED_APP_NAME = "\u30c6\u30b9\u30c8\u65e5\u672c\u8a9e";
    private static final String EXPECTED_CP932_APP_NAME = "\uff8c\uff67\uff72\uff85\uff99\uff8c\uff67\uff9d\uff80\uff7c\uff9e\u30fc\u2161";

    private JamMetadataEncodingProbe() {
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("jam-metadata-encoding");
        verifyJam(root.resolve("EncodingProbe.jam"), buildJam(EXPECTED_APP_NAME), DoJaEncoding.DEFAULT_CHARSET, EXPECTED_APP_NAME);
        verifyJam(root.resolve("Cp932EncodingProbe.jam"), buildJam(EXPECTED_CP932_APP_NAME), CP932, EXPECTED_CP932_APP_NAME);

        System.out.println("Jam metadata encoding probe OK");
    }

    private static void verifyJam(Path jam, String contents, Charset charset, String expectedAppName) throws Exception {
        Files.writeString(jam, contents, charset);
        LaunchConfig config = JamLauncher.buildLaunchConfig(jam, false);
        check(expectedAppName.equals(config.title()), "AppName should decode correctly for " + jam.getFileName());
        check(expectedAppName.equals(config.parameters().get("AppName")),
                "AppName parameter should preserve metadata text for " + jam.getFileName());
    }

    private static String buildJam(String appName) {
        return "AppClass=" + ProbeApp.class.getName() + '\n'
                + "AppName=" + appName + '\n'
                + "DrawArea=176x208\n";
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public static final class ProbeApp extends IApplication {
        @Override
        public void start() {
        }
    }
}
