package com.nttdocomo.lang;

/**
 * Base class for objects that represent data managed by handset-native
 * features and that must not be taken outside the handset.
 *
 * <p>{@code XObject} is a special object type introduced so that data managed
 * by native handset functions, such as phone-book data, cannot be exported
 * from an i-appli. Compared with ordinary object types, only limited
 * operations are allowed.</p>
 *
 * <p>Introduced in DoJa-3.0 (505i).</p>
 *
 * @see XString
 */
public abstract class XObject {
    private final Object value;

    /**
     * Creates an {@code XObject} that wraps the specified native-managed
     * value.
     *
     * @param value the wrapped value
     */
    protected XObject(Object value) {
        this.value = value;
    }

    /**
     * Returns the wrapped value for internal runtime use.
     *
     * @return the wrapped value
     */
    protected final Object value() {
        return value;
    }

    /**
     * Checks instance identity.
     * No equality check is performed on the contents of the object.
     * This method returns the same result as {@code this == obj}.
     *
     * @param obj the object to compare
     * @return {@code true} if this is the same object
     */
    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    /**
     * Gets the hash value of this object.
     * The object contents are not used to calculate the hash value.
     * The rules defined by {@link java.lang.Object#hashCode()} also apply to
     * this class and its subclasses.
     *
     * @return the hash value of this object
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Gets the string representation of this object.
     * The object contents are not reflected in the returned string.
     * This method returns the same value as {@link java.lang.Object#toString()}.
     *
     * @return the string representation of this object
     */
    @Override
    public final String toString() {
        return super.toString();
    }
}
