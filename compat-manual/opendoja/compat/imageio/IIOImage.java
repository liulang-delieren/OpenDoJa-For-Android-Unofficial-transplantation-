package opendoja.compat.imageio;

public class IIOImage {
    private final Object image;
    private final Object renderedImageMetadata;
    private final opendoja.compat.imageio.metadata.IIOMetadata metadata;

    public IIOImage(Object renderedImage, Object renderedImageMetadata, opendoja.compat.imageio.metadata.IIOMetadata metadata) {
        this.image = renderedImage;
        this.renderedImageMetadata = renderedImageMetadata;
        this.metadata = metadata;
    }

    public Object getImage() {
        return image;
    }

    public Object getRenderedImageMetadata() {
        return renderedImageMetadata;
    }

    public opendoja.compat.imageio.metadata.IIOMetadata getMetadata() {
        return metadata;
    }
}
