package com.nttdocomo.lang;

/**
 * Defines an {@link XObject} that represents string data managed by handset
 * native functions and that must not be taken outside the handset.
 *
 * <p>{@code XString} can be used as a string for low-level UI drawing and for
 * high-level UI labels, but its contents must not be exported as byte data and
 * cannot be inspected or compared in the same way as a normal
 * {@link String}.</p>
 *
 * <p>Introduced in DoJa-3.0 (505i).</p>
 *
 * @see XObject
 */
public final class XString extends XObject {
    /**
     * Creates an {@code XString} object from a normal string object.
     * Only trusted i-applis whose permission allows reference to or use of the
     * corresponding native data can call this constructor.
     *
     * @param value the string to represent
     * @throws NullPointerException if {@code value} is {@code null}
     *
     * <p>Introduced in DoJa-4.1 (902i).</p>
     */
    public XString(String value) {
        super(requireValue(value));
    }

    /**
     * Gets the length of the string represented by this {@code XString}.
     *
     * @return the string length
     */
    public int length() {
        return stringValue().length();
    }

    /**
     * Concatenates the string represented by the specified {@code XString} to
     * the end of the string represented by this {@code XString}.
     * A new {@code XString} object is always created, even when the length of
     * {@code other} is {@code 0}.
     *
     * @param other the string to concatenate to the end of this
     *              {@code XString}
     * @return a new {@code XString} that represents the concatenated string
     * @throws NullPointerException if {@code other} is {@code null}
     *
     * <p>Introduced in DoJa-4.1 (902i).</p>
     */
    public XString concat(XString other) {
        if (other == null) {
            throw new NullPointerException("other");
        }
        return new XString(stringValue() + other.stringValue());
    }

    private String stringValue() {
        return (String) value();
    }

    private static String requireValue(String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        return value;
    }
}
