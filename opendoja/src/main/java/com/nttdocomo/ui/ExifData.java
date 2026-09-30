package com.nttdocomo.ui;

import com.nttdocomo.device.location.Degree;
import com.nttdocomo.device.location.Location;
import com.nttdocomo.device.location.LocationProvider;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Defines Exif attributes.
 * Exif attributes are a collection of multiple tags.
 * In the current implementation, only the GPS Info Tag group is supported.
 */
public class ExifData {
    private static final int TAG_GPS_VERSION_ID = 0;
    private static final int TAG_GPS_LATITUDE_REF = 1;
    private static final int TAG_GPS_LATITUDE = 2;
    private static final int TAG_GPS_LONGITUDE_REF = 3;
    private static final int TAG_GPS_LONGITUDE = 4;
    private static final int TAG_GPS_ALTITUDE_REF = 5;
    private static final int TAG_GPS_ALTITUDE = 6;
    private static final int TAG_GPS_TIME_STAMP = 7;
    private static final int TAG_GPS_MAP_DATUM = 18;
    private static final int TAG_GPS_DATE_STAMP = 29;
    private static final DateTimeFormatter EXIF_DATE = DateTimeFormatter.ofPattern("uuuu:MM:dd");

    /** Constant indicating the GPS Info Tag group (=34853). */
    public static final int GPS_INFO_TAG = 34853;

    /** Constant indicating that value acquisition is supported (=1). */
    public static final int SUPPORT_GET = 1;

    /** Constant indicating that value setting is supported (=2). */
    public static final int SUPPORT_SET = 2;

    private final Map<Integer, StoredTag> tags = new LinkedHashMap<>();

    /**
     * Creates an Exif-attribute object.
     * In the current implementation, immediately after creation only the
     * {@code GPSVersionID} tag is stored.
     */
    public ExifData() {
        tags.put(TAG_GPS_VERSION_ID, new StoredTag(
                TAG_GPS_VERSION_ID,
                TagInfo.BYTE,
                SUPPORT_GET,
                new long[]{2, 2, 0, 0}
        ));
    }

    /**
     * Gets the support status of the specified tag.
     * If a value other than {@link #GPS_INFO_TAG} is specified for
     * {@code tagGroup}, or if a tag number not defined by the current
     * implementation is specified for {@code tagID}, this method returns 0.
     *
     * @param tagGroup the group of the tag whose support status is to be
     *                 obtained; in the current implementation, only
     *                 {@link #GPS_INFO_TAG} can be specified
     * @param tagID the tag number whose support status is to be obtained
     * @return {@link #SUPPORT_GET} if only acquisition is supported,
     *         {@code (SUPPORT_GET | SUPPORT_SET)} if both acquisition and
     *         setting are supported, or 0 if neither is supported
     */
    public static int getSupportStatus(int tagGroup, int tagID) {
        TagDefinition definition = definition(tagGroup, tagID);
        return definition == null ? 0 : definition.support;
    }

    /**
     * Sets the value to be stored in the specified integer
     * ({@code BYTE}, {@code SHORT}, {@code LONG}, {@code SLONG}) Exif tag.
     * If value setting is not supported for the specified tag, this method
     * does nothing.
     *
     * @param tagGroup the group of the tag to set; in the current
     *                 implementation, only {@link #GPS_INFO_TAG} can be
     *                 specified
     * @param tagID the tag number to set
     * @param values the values to set; specify {@code null} to delete the tag
     * @throws IllegalArgumentException if the specified tag is not an integer
     *         type, if the array length does not match the tag count, or if a
     *         value is outside the range allowed by the tag type
     */
    public void setIntegerTag(int tagGroup, int tagID, long[] values) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, false);
        if (definition == null) {
            return;
        }
        requireType(definition, TagKind.INTEGER);
        if (!definition.isSettable()) {
            return;
        }
        if (values == null) {
            tags.remove(tagID);
            return;
        }
        validateCount(definition, values.length);
        validateIntegerValues(definition.type, values);
        tags.put(tagID, new StoredTag(tagID, definition.type, definition.support, values.clone()));
    }

    /**
     * Gets the value stored in the specified integer
     * ({@code BYTE}, {@code SHORT}, {@code LONG}, {@code SLONG}) Exif tag.
     * If value acquisition is not supported for the specified tag, or if the
     * tag does not exist, {@code null} is returned.
     *
     * @param tagGroup the group of the tag whose value is to be obtained; in
     *                 the current implementation, only {@link #GPS_INFO_TAG}
     *                 can be specified
     * @param tagID the tag number whose value is to be obtained
     * @return the value stored in the specified tag, or {@code null}
     * @throws IllegalArgumentException if the specified tag is not an integer
     *         type
     */
    public long[] getIntegerTag(int tagGroup, int tagID) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, true);
        if (definition == null) {
            return null;
        }
        requireType(definition, TagKind.INTEGER);
        StoredTag stored = tags.get(tagID);
        return stored == null ? null : ((long[]) stored.value).clone();
    }

    /**
     * Sets the value to be stored in the specified rational
     * ({@code RATIONAL}, {@code SRATIONAL}) Exif tag.
     * If value setting is not supported for the specified tag, this method
     * does nothing.
     *
     * @param tagGroup the group of the tag to set; in the current
     *                 implementation, only {@link #GPS_INFO_TAG} can be
     *                 specified
     * @param tagID the tag number to set
     * @param rational the value to set as an array of fractions; specify
     *                 {@code null} to delete the tag
     * @throws NullPointerException if any element of {@code rational} is
     *         {@code null}
     * @throws IllegalArgumentException if the specified tag is not a rational
     *         type, if the array length does not match the tag count, if any
     *         element has fewer than two values, if any denominator is 0, or
     *         if a value is outside the range allowed by the tag type
     */
    public void setRationalTag(int tagGroup, int tagID, long[][] rational) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, false);
        if (definition == null) {
            return;
        }
        requireType(definition, TagKind.RATIONAL);
        if (!definition.isSettable()) {
            return;
        }
        if (rational == null) {
            tags.remove(tagID);
            return;
        }
        validateCount(definition, rational.length);
        long[][] copy = new long[rational.length][2];
        for (int i = 0; i < rational.length; i++) {
            long[] fraction = rational[i];
            if (fraction == null) {
                throw new NullPointerException("rational[" + i + "]");
            }
            if (fraction.length < 2) {
                throw new IllegalArgumentException("rational[" + i + "] must have at least two elements");
            }
            if (fraction[1] == 0L) {
                throw new IllegalArgumentException("rational[" + i + "] denominator must not be 0");
            }
            validateRationalValue(definition.type, fraction[0], fraction[1]);
            copy[i][0] = fraction[0];
            copy[i][1] = fraction[1];
        }
        tags.put(tagID, new StoredTag(tagID, definition.type, definition.support, copy));
    }

    /**
     * Gets the value stored in the specified rational
     * ({@code RATIONAL}, {@code SRATIONAL}) Exif tag.
     * If value acquisition is not supported for the specified tag, or if the
     * tag does not exist, {@code null} is returned.
     *
     * @param tagGroup the group of the tag whose value is to be obtained; in
     *                 the current implementation, only {@link #GPS_INFO_TAG}
     *                 can be specified
     * @param tagID the tag number whose value is to be obtained
     * @return the value stored in the specified tag, or {@code null}
     * @throws IllegalArgumentException if the specified tag is not a rational
     *         type
     */
    public long[][] getRationalTag(int tagGroup, int tagID) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, true);
        if (definition == null) {
            return null;
        }
        requireType(definition, TagKind.RATIONAL);
        StoredTag stored = tags.get(tagID);
        if (stored == null) {
            return null;
        }
        long[][] rational = (long[][]) stored.value;
        long[][] copy = new long[rational.length][2];
        for (int i = 0; i < rational.length; i++) {
            copy[i][0] = rational[i][0];
            copy[i][1] = rational[i][1];
        }
        return copy;
    }

    /**
     * Sets the value to be stored in the specified ASCII Exif tag.
     * If value setting is not supported for the specified tag, this method
     * does nothing.
     *
     * @param tagGroup the group of the tag to set; in the current
     *                 implementation, only {@link #GPS_INFO_TAG} can be
     *                 specified
     * @param tagID the tag number to set
     * @param value the value to set; do not include the terminating NUL;
     *              specify {@code null} to delete the tag
     * @throws IllegalArgumentException if the specified tag is not ASCII, if
     *         the string length does not match the required count, or if the
     *         string contains a non-ASCII code
     */
    public void setAsciiTag(int tagGroup, int tagID, String value) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, false);
        if (definition == null) {
            return;
        }
        requireType(definition, TagKind.ASCII);
        if (!definition.isSettable()) {
            return;
        }
        if (value == null) {
            tags.remove(tagID);
            return;
        }
        validateAsciiValue(definition, value);
        tags.put(tagID, new StoredTag(tagID, definition.type, definition.support, value));
    }

    /**
     * Gets the value stored in the specified ASCII Exif tag.
     * If value acquisition is not supported for the specified tag, or if the
     * tag does not exist, {@code null} is returned.
     *
     * @param tagGroup the group of the tag whose value is to be obtained; in
     *                 the current implementation, only {@link #GPS_INFO_TAG}
     *                 can be specified
     * @param tagID the tag number whose value is to be obtained
     * @return the value stored in the specified tag, or {@code null}
     * @throws IllegalArgumentException if the specified tag is not ASCII
     */
    public String getAsciiTag(int tagGroup, int tagID) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, true);
        if (definition == null) {
            return null;
        }
        requireType(definition, TagKind.ASCII);
        StoredTag stored = tags.get(tagID);
        return stored == null ? null : (String) stored.value;
    }

    /**
     * Sets the value to be stored in the specified UNDEFINED Exif tag.
     * If value setting is not supported for the specified tag, this method
     * does nothing.
     *
     * @param tagGroup the group of the tag to set; in the current
     *                 implementation, only {@link #GPS_INFO_TAG} can be
     *                 specified
     * @param tagID the tag number to set
     * @param value the value to set; specify {@code null} to delete the tag
     * @throws IllegalArgumentException if the specified tag is not UNDEFINED or
     *         if the array length does not match the required count
     */
    public void setUndefinedTag(int tagGroup, int tagID, byte[] value) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, false);
        if (definition == null) {
            return;
        }
        requireType(definition, TagKind.UNDEFINED);
        if (!definition.isSettable()) {
            return;
        }
        if (value == null) {
            tags.remove(tagID);
            return;
        }
        validateCount(definition, value.length);
        tags.put(tagID, new StoredTag(tagID, definition.type, definition.support, value.clone()));
    }

    /**
     * Gets the value stored in the specified UNDEFINED Exif tag.
     * If value acquisition is not supported for the specified tag, or if the
     * tag does not exist, {@code null} is returned.
     *
     * @param tagGroup the group of the tag whose value is to be obtained; in
     *                 the current implementation, only {@link #GPS_INFO_TAG}
     *                 can be specified
     * @param tagID the tag number whose value is to be obtained
     * @return the value stored in the specified tag, or {@code null}
     * @throws IllegalArgumentException if the specified tag is not UNDEFINED
     */
    public byte[] getUndefinedTag(int tagGroup, int tagID) {
        TagDefinition definition = requireDefinition(tagGroup, tagID, true);
        if (definition == null) {
            return null;
        }
        requireType(definition, TagKind.UNDEFINED);
        StoredTag stored = tags.get(tagID);
        return stored == null ? null : ((byte[]) stored.value).clone();
    }

    /**
     * Enumerates all Exif tags stored in this object.
     * All tags that have valid values set are returned, regardless of whether
     * they are acquisition-only or acquisition-and-setting supported.
     *
     * @return an {@code Enumeration} that can access all Exif tags stored in
     *         this object as {@link TagInfo} instances
     */
    public Enumeration<TagInfo> enumerateTags() {
        return new Enumeration<>() {
            private int nextTagId = Integer.MIN_VALUE;
            private final java.util.Set<Integer> delivered = new java.util.LinkedHashSet<>();

            @Override
            public boolean hasMoreElements() {
                nextTagId = findNextTagId();
                return nextTagId != Integer.MIN_VALUE;
            }

            @Override
            public TagInfo nextElement() {
                if (nextTagId == Integer.MIN_VALUE && !hasMoreElements()) {
                    throw new NoSuchElementException();
                }
                int tagId = nextTagId;
                delivered.add(tagId);
                nextTagId = Integer.MIN_VALUE;
                TagDefinition definition = definition(GPS_INFO_TAG, tagId);
                return new TagInfo(GPS_INFO_TAG, tagId, definition == null ? TagInfo.UNDEFINED : definition.type);
            }

            private int findNextTagId() {
                for (Integer tagId : tags.keySet()) {
                    if (!delivered.contains(tagId)) {
                        return tagId;
                    }
                }
                return Integer.MIN_VALUE;
            }
        };
    }

    /**
     * Creates a location-information object based on this Exif attribute.
     * Each time this method is called, a new {@link Location} object is
     * created and returned.
     * If even one of the required items is missing or invalid, {@code null} is
     * returned.
     *
     * @return the {@link Location} object based on this Exif attribute, or
     *         {@code null} if sufficient information is not set
     */
    public Location toLocation() {
        try {
            String latitudeRef = getAsciiTag(GPS_INFO_TAG, TAG_GPS_LATITUDE_REF);
            long[][] latitudeValue = getRationalTag(GPS_INFO_TAG, TAG_GPS_LATITUDE);
            String longitudeRef = getAsciiTag(GPS_INFO_TAG, TAG_GPS_LONGITUDE_REF);
            long[][] longitudeValue = getRationalTag(GPS_INFO_TAG, TAG_GPS_LONGITUDE);
            long[][] timeStamp = getRationalTag(GPS_INFO_TAG, TAG_GPS_TIME_STAMP);
            String mapDatum = getAsciiTag(GPS_INFO_TAG, TAG_GPS_MAP_DATUM);
            String dateStamp = getAsciiTag(GPS_INFO_TAG, TAG_GPS_DATE_STAMP);
            if (latitudeRef == null || latitudeValue == null
                    || longitudeRef == null || longitudeValue == null
                    || timeStamp == null || mapDatum == null || dateStamp == null) {
                return null;
            }

            Degree latitude = decodeCoordinate(latitudeRef, latitudeValue, true);
            Degree longitude = decodeCoordinate(longitudeRef, longitudeValue, false);
            int datum = decodeDatum(mapDatum);
            long timestamp = decodeTimestamp(dateStamp, timeStamp);
            int altitude = decodeAltitude();

            return new Location(latitude, longitude, altitude, datum, timestamp, Location.ACCURACY_UNKNOWN);
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            return null;
        }
    }

    /**
     * Reflects the specified location information in this Exif attribute.
     * Based on the latitude, longitude, altitude, datum, and positioning date
     * and time of the specified location information, the corresponding GPS
     * tags are updated or added.
     * If altitude is {@link Location#ALTITUDE_UNKNOWN}, the
     * {@code GPSAltitude} and {@code GPSAltitudeRef} tags are deleted.
     * Horizontal-accuracy information is ignored.
     *
     * @param location the location information to reflect in this Exif
     *                 attribute
     * @throws NullPointerException if {@code location} is {@code null}
     */
    public void update(Location location) {
        if (location == null) {
            throw new NullPointerException("location");
        }

        Degree latitude = location.getLatitude();
        Degree longitude = location.getLongitude();
        setAsciiTag(GPS_INFO_TAG, TAG_GPS_LATITUDE_REF,
                latitude.getFloatingPointNumber() < 0.0d ? "S" : "N");
        setRationalTag(GPS_INFO_TAG, TAG_GPS_LATITUDE, encodeCoordinate(latitude));
        setAsciiTag(GPS_INFO_TAG, TAG_GPS_LONGITUDE_REF,
                longitude.getFloatingPointNumber() < 0.0d ? "W" : "E");
        setRationalTag(GPS_INFO_TAG, TAG_GPS_LONGITUDE, encodeCoordinate(longitude));

        if (location.getAltitude() == Location.ALTITUDE_UNKNOWN) {
            setIntegerTag(GPS_INFO_TAG, TAG_GPS_ALTITUDE_REF, null);
            setRationalTag(GPS_INFO_TAG, TAG_GPS_ALTITUDE, null);
        } else {
            int altitude = location.getAltitude();
            setIntegerTag(GPS_INFO_TAG, TAG_GPS_ALTITUDE_REF, new long[]{altitude < 0 ? 1 : 0});
            setRationalTag(GPS_INFO_TAG, TAG_GPS_ALTITUDE,
                    new long[][]{{Math.abs((long) altitude), 1L}});
        }

        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(location.getTimestamp()), ZoneOffset.UTC);
        setRationalTag(GPS_INFO_TAG, TAG_GPS_TIME_STAMP, new long[][]{
                {dateTime.getHour(), 1L},
                {dateTime.getMinute(), 1L},
                {dateTime.getSecond() * 1000L + dateTime.getNano() / 1_000_000L, 1000L}
        });
        setAsciiTag(GPS_INFO_TAG, TAG_GPS_MAP_DATUM,
                location.getDatum() == LocationProvider.DATUM_TOKYO ? "TOKYO" : "WGS-84");
        setAsciiTag(GPS_INFO_TAG, TAG_GPS_DATE_STAMP, EXIF_DATE.format(dateTime.toLocalDate()));
    }

    private static TagDefinition requireDefinition(int tagGroup, int tagID, boolean allowMissing) {
        TagDefinition definition = definition(tagGroup, tagID);
        if (definition == null) {
            if (allowMissing) {
                return null;
            }
            return null;
        }
        return definition;
    }

    private static TagDefinition definition(int tagGroup, int tagID) {
        if (tagGroup != GPS_INFO_TAG) {
            return null;
        }
        return switch (tagID) {
            case TAG_GPS_VERSION_ID -> new TagDefinition(tagID, TagInfo.BYTE, 4, SUPPORT_GET, TagKind.INTEGER);
            case TAG_GPS_LATITUDE_REF -> new TagDefinition(tagID, TagInfo.ASCII, 2, SUPPORT_GET | SUPPORT_SET, TagKind.ASCII);
            case TAG_GPS_LATITUDE -> new TagDefinition(tagID, TagInfo.RATIONAL, 3, SUPPORT_GET | SUPPORT_SET, TagKind.RATIONAL);
            case TAG_GPS_LONGITUDE_REF -> new TagDefinition(tagID, TagInfo.ASCII, 2, SUPPORT_GET | SUPPORT_SET, TagKind.ASCII);
            case TAG_GPS_LONGITUDE -> new TagDefinition(tagID, TagInfo.RATIONAL, 3, SUPPORT_GET | SUPPORT_SET, TagKind.RATIONAL);
            case TAG_GPS_ALTITUDE_REF -> new TagDefinition(tagID, TagInfo.BYTE, 1, SUPPORT_GET | SUPPORT_SET, TagKind.INTEGER);
            case TAG_GPS_ALTITUDE -> new TagDefinition(tagID, TagInfo.RATIONAL, 1, SUPPORT_GET | SUPPORT_SET, TagKind.RATIONAL);
            case TAG_GPS_TIME_STAMP -> new TagDefinition(tagID, TagInfo.RATIONAL, 3, SUPPORT_GET | SUPPORT_SET, TagKind.RATIONAL);
            case TAG_GPS_MAP_DATUM -> new TagDefinition(tagID, TagInfo.ASCII, -1, SUPPORT_GET | SUPPORT_SET, TagKind.ASCII);
            case TAG_GPS_DATE_STAMP -> new TagDefinition(tagID, TagInfo.ASCII, 11, SUPPORT_GET | SUPPORT_SET, TagKind.ASCII);
            default -> null;
        };
    }

    private static void requireType(TagDefinition definition, TagKind expectedKind) {
        if (definition.kind != expectedKind) {
            throw new IllegalArgumentException("Tag " + definition.tagID + " is not of the requested type");
        }
    }

    private static void validateCount(TagDefinition definition, int actualCount) {
        if (definition.count >= 0 && definition.count != actualCount) {
            throw new IllegalArgumentException("Tag " + definition.tagID + " requires count " + definition.count);
        }
    }

    private static void validateIntegerValues(int type, long[] values) {
        for (long value : values) {
            switch (type) {
                case TagInfo.BYTE -> {
                    if (value < 0L || value > 255L) {
                        throw new IllegalArgumentException("BYTE value out of range: " + value);
                    }
                }
                case TagInfo.SHORT -> {
                    if (value < 0L || value > 65535L) {
                        throw new IllegalArgumentException("SHORT value out of range: " + value);
                    }
                }
                case TagInfo.LONG -> {
                    if (value < 0L || value > 0xFFFF_FFFFL) {
                        throw new IllegalArgumentException("LONG value out of range: " + value);
                    }
                }
                case TagInfo.SLONG -> {
                    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("SLONG value out of range: " + value);
                    }
                }
                default -> throw new IllegalArgumentException("Unsupported integer type: " + type);
            }
        }
    }

    private static void validateRationalValue(int type, long numerator, long denominator) {
        if (type == TagInfo.RATIONAL) {
            if (numerator < 0L || denominator < 0L
                    || numerator > 0xFFFF_FFFFL || denominator > 0xFFFF_FFFFL) {
                throw new IllegalArgumentException("RATIONAL value out of range");
            }
            return;
        }
        if (type == TagInfo.SRATIONAL) {
            if (numerator < Integer.MIN_VALUE || numerator > Integer.MAX_VALUE
                    || denominator < Integer.MIN_VALUE || denominator > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("SRATIONAL value out of range");
            }
            return;
        }
        throw new IllegalArgumentException("Unsupported rational type: " + type);
    }

    private static void validateAsciiValue(TagDefinition definition, String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > 0x7F) {
                throw new IllegalArgumentException("Non-ASCII character at index " + i);
            }
        }
        if (definition.tagID == TAG_GPS_MAP_DATUM) {
            if (!"TOKYO".equals(value) && !"WGS-84".equals(value)) {
                throw new IllegalArgumentException("Unsupported GPSMapDatum value: " + value);
            }
            return;
        }
        if (definition.count >= 0 && value.length() != definition.count - 1) {
            throw new IllegalArgumentException("Tag " + definition.tagID + " requires length " + (definition.count - 1));
        }
    }

    private Degree decodeCoordinate(String ref, long[][] values, boolean latitude) {
        double coordinate = 0.0d;
        for (int i = 0; i < values.length; i++) {
            long[] fraction = values[i];
            if (fraction == null || fraction.length < 2 || fraction[1] == 0L) {
                throw new IllegalArgumentException("Invalid coordinate fraction");
            }
            double part = ((double) fraction[0]) / ((double) fraction[1]);
            coordinate += switch (i) {
                case 0 -> part;
                case 1 -> part / 60.0d;
                default -> part / 3600.0d;
            };
        }
        boolean negative = switch (ref) {
            case "N" -> false;
            case "S" -> true;
            case "E" -> false;
            case "W" -> true;
            default -> throw new IllegalArgumentException("Invalid coordinate reference: " + ref);
        };
        if (latitude && (negative ? "S" : "N").charAt(0) != ref.charAt(0)) {
            throw new IllegalArgumentException("Latitude reference mismatch");
        }
        if (!latitude && (negative ? "W" : "E").charAt(0) != ref.charAt(0)) {
            throw new IllegalArgumentException("Longitude reference mismatch");
        }
        return new Degree(negative ? -coordinate : coordinate);
    }

    private int decodeDatum(String mapDatum) {
        return switch (mapDatum) {
            case "TOKYO" -> LocationProvider.DATUM_TOKYO;
            case "WGS-84" -> LocationProvider.DATUM_WGS84;
            default -> throw new IllegalArgumentException("Invalid GPSMapDatum: " + mapDatum);
        };
    }

    private long decodeTimestamp(String dateStamp, long[][] timeStamp) {
        if (timeStamp.length != 3) {
            throw new IllegalArgumentException("GPSTimeStamp must contain three elements");
        }
        LocalDate date = LocalDate.parse(dateStamp, EXIF_DATE);
        int hour = decodeWholeRational(timeStamp[0], "hour");
        int minute = decodeWholeRational(timeStamp[1], "minute");
        long[] secondsValue = timeStamp[2];
        if (secondsValue == null || secondsValue.length < 2 || secondsValue[1] == 0L) {
            throw new IllegalArgumentException("Invalid GPSTimeStamp seconds");
        }
        double seconds = ((double) secondsValue[0]) / ((double) secondsValue[1]);
        int secondWhole = (int) seconds;
        int millis = (int) java.lang.Math.round((seconds - secondWhole) * 1000.0d);
        if (millis == 1000) {
            secondWhole++;
            millis = 0;
        }
        LocalTime time = LocalTime.of(hour, minute, secondWhole, millis * 1_000_000);
        return LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private int decodeAltitude() {
        long[] altitudeRef = getIntegerTag(GPS_INFO_TAG, TAG_GPS_ALTITUDE_REF);
        long[][] altitudeValue = getRationalTag(GPS_INFO_TAG, TAG_GPS_ALTITUDE);
        if (altitudeRef == null || altitudeRef.length == 0 || altitudeValue == null || altitudeValue.length == 0) {
            return Location.ALTITUDE_UNKNOWN;
        }
        if (altitudeRef[0] != 0L && altitudeRef[0] != 1L) {
            return Location.ALTITUDE_UNKNOWN;
        }
        long[] fraction = altitudeValue[0];
        if (fraction == null || fraction.length < 2 || fraction[1] == 0L) {
            return Location.ALTITUDE_UNKNOWN;
        }
        int altitude = (int) java.lang.Math.round(((double) fraction[0]) / ((double) fraction[1]));
        return altitudeRef[0] == 1L ? -altitude : altitude;
    }

    private static int decodeWholeRational(long[] fraction, String label) {
        if (fraction == null || fraction.length < 2 || fraction[1] == 0L) {
            throw new IllegalArgumentException("Invalid " + label);
        }
        double value = ((double) fraction[0]) / ((double) fraction[1]);
        return (int) java.lang.Math.round(value);
    }

    private static long[][] encodeCoordinate(Degree degree) {
        double absolute = java.lang.Math.abs(degree.getFloatingPointNumber());
        long degrees = (long) java.lang.Math.floor(absolute);
        double minutesValue = (absolute - degrees) * 60.0d;
        long minutes = (long) java.lang.Math.floor(minutesValue);
        double secondsValue = (minutesValue - minutes) * 60.0d;
        long secondsScaled = java.lang.Math.round(secondsValue * 1000.0d);
        return new long[][]{
                {degrees, 1L},
                {minutes, 1L},
                {secondsScaled, 1000L}
        };
    }

    /**
     * Defines the inner class that represents the tag information included in
     * the return value of {@link #enumerateTags()}.
     */
    public static class TagInfo {
        /** Constant indicating the BYTE tag type (=1). */
        public static final int BYTE = 1;
        /** Constant indicating the ASCII tag type (=2). */
        public static final int ASCII = 2;
        /** Constant indicating the SHORT tag type (=3). */
        public static final int SHORT = 3;
        /** Constant indicating the LONG tag type (=4). */
        public static final int LONG = 4;
        /** Constant indicating the RATIONAL tag type (=5). */
        public static final int RATIONAL = 5;
        /** Constant indicating the UNDEFINED tag type (=7). */
        public static final int UNDEFINED = 7;
        /** Constant indicating the SLONG tag type (=9). */
        public static final int SLONG = 9;
        /** Constant indicating the SRATIONAL tag type (=10). */
        public static final int SRATIONAL = 10;

        private final int tagGroup;
        private final int tagID;
        private final int type;

        private TagInfo(int tagGroup, int tagID, int type) {
            this.tagGroup = tagGroup;
            this.tagID = tagID;
            this.type = type;
        }

        /**
         * Gets the group to which this tag belongs.
         *
         * @return the tag group
         */
        public int getTagGroup() {
            return tagGroup;
        }

        /**
         * Gets the tag number of this tag.
         *
         * @return the tag number
         */
        public int getTagID() {
            return tagID;
        }

        /**
         * Gets the type of this tag.
         *
         * @return the tag type
         */
        public int getType() {
            return type;
        }
    }

    private enum TagKind {
        INTEGER,
        RATIONAL,
        ASCII,
        UNDEFINED
    }

    private record TagDefinition(int tagID, int type, int count, int support, TagKind kind) {
        private boolean isSettable() {
            return (support & SUPPORT_SET) != 0;
        }
    }

    private record StoredTag(int tagID, int type, int support, Object value) {
    }
}
