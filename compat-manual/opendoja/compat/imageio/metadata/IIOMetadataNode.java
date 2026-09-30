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