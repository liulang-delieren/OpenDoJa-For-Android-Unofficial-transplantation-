package opendoja.host;

import com.nttdocomo.ui.Frame;

import opendoja.compat.awt.*;
import opendoja.compat.awt.image.BufferedImage;

public interface HostOverlayRenderer {
    void paint(Graphics2D graphics, ExternalFrameLayout layout, Frame frame, BufferedImage drawImage);
}
