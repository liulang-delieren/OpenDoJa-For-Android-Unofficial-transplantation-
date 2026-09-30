package opendoja.host;

import opendoja.compat.awt.*;

public record ExternalFrameLayout(
        boolean enabled,
        double scale,
        Rectangle screenArea,
        Rectangle drawArea,
        Rectangle topBar,
        Rectangle bottomBar,
        Rectangle leftConnector,
        Rectangle rightConnector,
        Rectangle statusArea,
        Rectangle softKeyArea,
        Dimension preferredSize
) {
}
