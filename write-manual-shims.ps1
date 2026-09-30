$ErrorActionPreference = 'Stop'
$enc = [Text.UTF8Encoding]::new($false)
$m = Join-Path $PSScriptRoot 'compat-manual\opendoja\compat'
function W($rel, $content) {
    $p = Join-Path $m $rel
    $d = Split-Path $p -Parent
    if (-not (Test-Path $d)) { New-Item -ItemType Directory -Force -Path $d | Out-Null }
    [IO.File]::WriteAllText($p, $content.Replace("`r`n", "`n").Replace("`n", "`r`n"), $enc)
    Write-Output ("w " + $rel)
}

W 'imageio\ImageIO.java' @'
package opendoja.compat.imageio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;

public final class ImageIO {
    private ImageIO() {
    }

    public static opendoja.compat.awt.image.BufferedImage read(InputStream input) throws IOException {
        return null;
    }

    public static boolean write(opendoja.compat.awt.image.RenderedImage image, String formatName, OutputStream output) throws IOException {
        return false;
    }

    public static Iterator<ImageWriter> getImageWritersByFormatName(String formatName) {
        return Collections.emptyIterator();
    }

    public static Iterator<ImageReader> getImageReaders(Object input) {
        return Collections.emptyIterator();
    }

    public static opendoja.compat.imageio.stream.ImageInputStream createImageInputStream(Object input) throws IOException {
        return null;
    }
}
'@

W 'imageio\ImageReader.java' @'
package opendoja.compat.imageio;

import java.io.IOException;

public class ImageReader {
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
    }

    public opendoja.compat.imageio.metadata.IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    public opendoja.compat.awt.image.BufferedImage read(int imageIndex) throws IOException {
        return null;
    }

    public opendoja.compat.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    public void dispose() {
    }
}
'@

W 'imageio\ImageWriter.java' @'
package opendoja.compat.imageio;

import java.io.IOException;

public class ImageWriter {
    public void setOutput(Object output) {
    }

    public ImageWriteParam getDefaultWriteParam() {
        return new ImageWriteParam();
    }

    public void write(opendoja.compat.awt.image.RenderedImage image, IIOImage imageObj, ImageWriteParam param) throws IOException {
    }

    public void dispose() {
    }
}
'@

W 'imageio\IIOImage.java' @'
package opendoja.compat.imageio;

public class IIOImage {
    public IIOImage(Object renderedImage, Object renderedImageMetadata, opendoja.compat.imageio.metadata.IIOMetadata metadata) {
    }
}
'@

W 'imageio\ImageWriteParam.java' @'
package opendoja.compat.imageio;

public class ImageWriteParam {
    public static final int MODE_COPY_FROM_METADATA = -1;
    public static final int MODE_DEFAULT = 0;
    public static final int MODE_EXPLICIT = 1;

    public boolean canWriteCompressed() {
        return false;
    }

    public void setCompressionMode(int mode) {
    }

    public void setCompressionQuality(float quality) {
    }
}
'@

W 'imageio\metadata\IIOMetadata.java' @'
package opendoja.compat.imageio.metadata;

public class IIOMetadata {
    public String[] getMetadataFormatNames() {
        return null;
    }

    public org.w3c.dom.Node getAsTree(String formatName) {
        return null;
    }
}
'@

W 'imageio\metadata\IIOMetadataNode.java' @'
package opendoja.compat.imageio.metadata;

public class IIOMetadataNode {
    private final String name;

    public IIOMetadataNode() {
        this(null);
    }

    public IIOMetadataNode(String name) {
        this.name = name;
    }

    public String getNodeName() {
        return name == null ? "" : name;
    }

    public String getAttribute(String name) {
        return "";
    }

    public org.w3c.dom.Node getFirstChild() {
        return null;
    }
}
'@

W 'imageio\stream\ImageInputStream.java' @'
package opendoja.compat.imageio.stream;

public interface ImageInputStream extends java.io.Closeable {
}
'@

W 'imageio\stream\MemoryCacheImageOutputStream.java' @'
package opendoja.compat.imageio.stream;

import java.io.IOException;
import java.io.OutputStream;

public class MemoryCacheImageOutputStream implements ImageInputStream {
    public MemoryCacheImageOutputStream(OutputStream stream) {
    }

    @Override
    public void close() throws IOException {
    }
}
'@

W 'management\ManagementFactory.java' @'
package opendoja.compat.management;

public final class ManagementFactory {
    private ManagementFactory() {
    }

    public static RuntimeMXBean getRuntimeMXBean() {
        return new RuntimeMXBean();
    }
}
'@

W 'management\RuntimeMXBean.java' @'
package opendoja.compat.management;

import java.util.Collections;
import java.util.List;

public class RuntimeMXBean {
    public List<String> getInputArguments() {
        return Collections.emptyList();
    }
}
'@

W 'naming\InvalidNameException.java' @'
package opendoja.compat.naming;

public class InvalidNameException extends Exception {
    public InvalidNameException() {
        super();
    }

    public InvalidNameException(String message) {
        super(message);
    }
}
'@

W 'naming\LdapName.java' @'
package opendoja.compat.naming;

import java.util.Collections;
import java.util.List;

public class LdapName {
    public LdapName(String name) throws InvalidNameException {
    }

    public List<Rdn> getRdns() {
        return Collections.emptyList();
    }
}
'@

W 'naming\Rdn.java' @'
package opendoja.compat.naming;

public class Rdn {
    public String getType() {
        return null;
    }

    public Object getValue() {
        return null;
    }
}
'@
