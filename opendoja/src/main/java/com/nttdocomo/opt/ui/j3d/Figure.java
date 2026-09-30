package com.nttdocomo.opt.ui.j3d;

import opendoja.g3d.MascotFigure;
import opendoja.g3d.MascotLoader;
import opendoja.g3d.SoftwareTexture;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a class that holds model data.
 * Model data is read from MBAC data and retained by this class.
 * The default posture is the base posture defined by the MBAC coordinates, but
 * the posture can be changed with an {@link ActionTable}. From DoJa-3.0
 * onward, appearance-state changes through vertex animation are also
 * supported.
 *
 * <p>Before drawing, a texture must be set unless the model consists entirely
 * of color polygons.</p>
 *
 * <p>Introduced in DoJa-2.0.</p>
 */
public class Figure {
    private final MascotFigure handle;

    /**
     * Creates a model object from MBAC data.
     *
     * @param data the byte sequence that represents the MBAC data
     * @throws NullPointerException if {@code data} is {@code null}
     * @throws RuntimeException if the MBAC data is invalid
     */
    public Figure(byte[] data) {
        try {
            this.handle = new MascotFigure(MascotLoader.loadFigure(data));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a model object from MBAC data.
     *
     * @param inputStream the input stream object used to obtain the MBAC data
     * @throws NullPointerException if {@code inputStream} is {@code null}
     * @throws IOException if an I/O error occurs while reading the MBAC data
     * @throws RuntimeException if the MBAC data is invalid
     */
    public Figure(InputStream inputStream) throws IOException {
        this.handle = new MascotFigure(MascotLoader.loadFigure(inputStream));
    }

    /**
     * Sets the texture mapped onto the model.
     * A texture must be set before drawing the model.
     * From DoJa-3.0 onward, this is the same as calling
     * {@link #setTexture(Texture[])} with an array of length 1.
     *
     * @param texture the texture object; it must have been created for model
     *                mapping
     * @throws NullPointerException if {@code texture} is {@code null}
     */
    public void setTexture(Texture texture) {
        if (texture == null) {
            throw new NullPointerException("texture");
        }
        handle.setTexture(texture.handle());
    }

    /**
     * Sets the textures mapped onto the model.
     * Use this method to set multiple textures for model data that references
     * multiple textures.
     *
     * @param textures the array of texture objects; each element must have been
     *                 created for model mapping
     * @throws NullPointerException if {@code textures} is {@code null} or if
     *         any element is {@code null}
     */
    public void setTexture(Texture[] textures) {
        if (textures == null) {
            throw new NullPointerException("textures");
        }
        SoftwareTexture[] converted = new SoftwareTexture[textures.length];
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == null) {
                throw new NullPointerException("textures[" + i + "]");
            }
            converted[i] = textures[i].handle();
        }
        handle.setTextures(converted);
    }

    /**
     * Gets the number of textures required by the model.
     *
     * @return the number of textures
     */
    public int getNumTextures() {
        return handle.numTextures();
    }

    /**
     * Changes the texture selection of the model.
     *
     * @param index the texture index to select
     */
    public void changeTexture(int index) {
        handle.selectTexture(index);
    }

    /**
     * Sets the posture of the model.
     * The model retains the posture until this method is called again.
     * From DoJa-3.0 onward, the appearance state is also set at the same time.
     *
     * @param actionTable the action-table object that contains the posture to
     *                    set
     * @param action the index of the action whose posture is to be set
     * @param frame the frame whose posture is to be set
     * @throws NullPointerException if {@code actionTable} is {@code null}
     * @throws IllegalArgumentException if {@code action} is invalid
     */
    public void setPosture(ActionTable actionTable, int action, int frame) {
        handle.setAction(actionTable.handle(), action);
        handle.setTime(frame);
    }

    /**
     * Gets the number of appearance states of the model.
     *
     * @return the number of appearance states of the model
     */
    public int getNumPattern() {
        return handle.numPatterns();
    }

    /**
     * Sets the appearance state of the model.
     *
     * @param pattern the appearance state of the model
     */
    public void setPattern(int pattern) {
        handle.setPattern(pattern);
    }

    MascotFigure handle() {
        return handle;
    }
}
