package opendoja.launcher;

import opendoja.host.JamGameJarLocator;

import java.io.IOException;
import java.nio.file.Path;

final class JamGameJarResolver {
    GameLaunchSelection resolve(Path jamPath) throws IOException {
        Path normalizedJam = jamPath.toAbsolutePath().normalize();
        return new GameLaunchSelection(normalizedJam, JamGameJarLocator.locate(normalizedJam));
    }
}
