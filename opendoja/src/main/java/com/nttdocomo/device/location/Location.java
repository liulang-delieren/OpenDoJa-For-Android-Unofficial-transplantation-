package com.nttdocomo.device.location;

import com.nttdocomo.net.URLDecoder;
import com.nttdocomo.net.URLEncoder;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a location result.
 * This object stores location information such as latitude, longitude,
 * altitude, geodetic datum, horizontal accuracy, and the positioning time.
 */
public class Location {
    /** The worst accuracy value corresponding to positioning level 1, in meters (=2147483647). */
    public static final int ACCURACY_COARSE = Integer.MAX_VALUE;

    /** The worst accuracy value corresponding to positioning level 2, in meters (=299). */
    public static final int ACCURACY_NORMAL = 299;

    /** The worst accuracy value corresponding to positioning level 3, in meters (=49). */
    public static final int ACCURACY_FINE = 49;

    /** Indicates that the accuracy is unknown (=-1). */
    public static final int ACCURACY_UNKNOWN = -1;

    /** Indicates that the altitude is unknown (=0x80000000). */
    public static final int ALTITUDE_UNKNOWN = Integer.MIN_VALUE;

    /** Indicates that N/S/W/E is used as the prefix for latitude/longitude strings (=0). */
    public static final int PREFIX_DIRECTION = 0;

    /** Indicates that (+)/- is used as the prefix for latitude/longitude strings (=1). */
    public static final int PREFIX_SIGN = 1;

    private static final Pattern DEGREE_COORDINATE = Pattern.compile("^([NSEW\\-]?)(\\d+)\\.(\\d{6})$");
    private static final Pattern SIGNED_DEGREE_COORDINATE = Pattern.compile("^([+\\-])(\\d+)\\.(\\d{6})$");
    private static final Pattern DMS_COORDINATE = Pattern.compile("^([NSEW\\-]?)(\\d+)\\.(\\d{2})\\.(\\d{2})\\.(\\d{2})$");
    private static final Pattern DMS2_COORDINATE = Pattern.compile("^([NSEW+\\-])(\\d{2,3})\\.(\\d{2})\\.(\\d{2})\\.(\\d{3})$");
    private static final Pattern MOPA_COMBINED = Pattern.compile("^([NS].+?)([EW].+)$");
    private static final double WGS84_RADIUS_METERS = 6378137.0d;
    private static final double TOKYO_RADIUS_METERS = 6377397.155d;

    private Degree latitude;
    private Degree longitude;
    private int altitude;
    private final int datum;
    private final long timestamp;
    private final int accuracy;

    /**
     * Creates this object by specifying latitude and longitude.
     * This is equivalent to calling
     * {@code Location(latitude, longitude, ALTITUDE_UNKNOWN, LocationProvider.DATUM_WGS84, System.currentTimeMillis(), 0)}.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @throws NullPointerException if {@code latitude} or {@code longitude} is {@code null}
     * @throws IllegalArgumentException if {@code latitude} is outside
     *         {@code [-90 degrees, 90 degrees]} or if {@code longitude} is
     *         outside {@code [-180 degrees, 180 degrees]}
     */
    public Location(Degree latitude, Degree longitude) {
        this(latitude, longitude, ALTITUDE_UNKNOWN, LocationProvider.DATUM_WGS84,
                System.currentTimeMillis(), 0);
    }

    /**
     * Creates this object by specifying a location-information URL.
     * This is equivalent to calling
     * {@code Location(url, ALTITUDE_UNKNOWN, System.currentTimeMillis())}.
     *
     * @param url the location-information URL or only its query string
     * @throws NullPointerException if {@code url} is {@code null}
     * @throws IllegalArgumentException if {@code url} cannot be interpreted as
     *         a location-information URL or query string
     */
    public Location(String url) {
        this(url, ALTITUDE_UNKNOWN, System.currentTimeMillis());
    }

    /**
     * Creates this object by specifying latitude, longitude, altitude, datum,
     * positioning time, and horizontal accuracy.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @param altitude the altitude in meters, or {@link #ALTITUDE_UNKNOWN}
     * @param datum the datum on which the specified values are based
     * @param timestamp the positioning time as the difference from
     *        1970-01-01 00:00:00 GMT
     * @param accuracy the horizontal accuracy in meters, or
     *        {@link #ACCURACY_UNKNOWN}; specify 0 when it is less than 1 meter
     * @throws NullPointerException if {@code latitude} or {@code longitude} is {@code null}
     * @throws IllegalArgumentException if {@code datum} is invalid, if
     *         {@code accuracy} is less than -1, or if latitude/longitude are
     *         outside the supported range
     */
    public Location(Degree latitude, Degree longitude, int altitude, int datum, long timestamp, int accuracy) {
        validateDatum(datum);
        validateAccuracy(accuracy);
        this.latitude = validateLatitude(latitude);
        this.longitude = normalizeLongitude(validateLongitude(longitude));
        this.altitude = altitude;
        this.datum = datum;
        this.timestamp = timestamp;
        this.accuracy = accuracy;
    }

    /**
     * Creates this object by specifying latitude, longitude, altitude, datum,
     * positioning time, and horizontal accuracy.
     * The latitude and longitude strings may be any format defined by
     * {@link #getLatitudeString(int, int)} and {@link #getLongitudeString(int, int)}.
     *
     * @param latitude the latitude string
     * @param longitude the longitude string
     * @param altitude the altitude in meters, or {@link #ALTITUDE_UNKNOWN}
     * @param datum the datum on which the specified values are based
     * @param timestamp the positioning time as the difference from
     *        1970-01-01 00:00:00 GMT
     * @param accuracy the horizontal accuracy in meters, or
     *        {@link #ACCURACY_UNKNOWN}
     * @throws NullPointerException if {@code latitude} or {@code longitude} is {@code null}
     * @throws IllegalArgumentException if the datum or accuracy is invalid, if
     *         a string has an invalid format, or if latitude/longitude are
     *         outside the supported range
     */
    public Location(String latitude, String longitude, int altitude, int datum, long timestamp, int accuracy) {
        this(parseCoordinateString(latitude, Axis.LATITUDE),
                parseCoordinateString(longitude, Axis.LONGITUDE),
                altitude, datum, timestamp, accuracy);
    }

    /**
     * Creates this object by specifying a location-information URL, a default
     * altitude, and a positioning time.
     * As the query-string format, the formats described by this class
     * (i Navi Link/MOPA format and Joint RFI format) can be specified.
     *
     * @param url the location-information URL or only its query string
     * @param altitude the default altitude in meters, or
     *        {@link #ALTITUDE_UNKNOWN}
     * @param timestamp the positioning time as the difference from
     *        1970-01-01 00:00:00 GMT
     * @throws NullPointerException if {@code url} is {@code null}
     * @throws IllegalArgumentException if {@code url} cannot be interpreted as
     *         a location-information URL or query string
     */
    public Location(String url, int altitude, long timestamp) {
        ParsedLocation parsed = parseLocationUrl(url, altitude);
        this.latitude = parsed.latitude;
        this.longitude = parsed.longitude;
        this.altitude = parsed.altitude;
        this.datum = parsed.datum;
        this.timestamp = timestamp;
        this.accuracy = parsed.accuracy;
    }

    /**
     * Gets the positioning time in milliseconds.
     *
     * @return the positioning time in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the datum on which this object's latitude, longitude, and altitude
     * are based.
     *
     * @return the datum on which this object is based
     */
    public int getDatum() {
        return datum;
    }

    /**
     * Gets a {@code Location} object obtained by converting this object to the
     * specified datum.
     * This method has no side effects.
     * Latitude, longitude, and altitude are handled in DEGREE units during the
     * calculation.
     *
     * @param datum the datum to convert to
     * @return the converted location
     * @throws IllegalArgumentException if {@code datum} is invalid
     */
    public Location transform(int datum) {
        validateDatum(datum);
        if (datum == this.datum) {
            return new Location(latitude, longitude, altitude, datum, timestamp, accuracy);
        }

        double sourceLatitude = latitude.getFloatingPointNumber();
        double sourceLongitude = longitude.getFloatingPointNumber();
        double transformedLatitude;
        double transformedLongitude;

        if (this.datum == LocationProvider.DATUM_TOKYO && datum == LocationProvider.DATUM_WGS84) {
            transformedLatitude = sourceLatitude - sourceLatitude * 0.00010695d
                    + sourceLongitude * 0.000017464d + 0.0046017d;
            transformedLongitude = sourceLongitude - sourceLatitude * 0.000046038d
                    - sourceLongitude * 0.000083043d + 0.010040d;
        } else if (this.datum == LocationProvider.DATUM_WGS84 && datum == LocationProvider.DATUM_TOKYO) {
            transformedLatitude = sourceLatitude + sourceLatitude * 0.00010695d
                    - sourceLongitude * 0.000017464d - 0.0046017d;
            transformedLongitude = sourceLongitude + sourceLatitude * 0.000046038d
                    + sourceLongitude * 0.000083043d - 0.010040d;
        } else {
            throw new IllegalArgumentException("datum out of range: " + datum);
        }

        return new Location(new Degree(transformedLatitude), new Degree(transformedLongitude),
                altitude, datum, timestamp, accuracy);
    }

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    public Degree getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude.
     *
     * @param latitude the latitude
     * @throws NullPointerException if {@code latitude} is {@code null}
     * @throws IllegalArgumentException if the specified value is outside
     *         {@code [-90 degrees, 90 degrees]}
     */
    public void setLatitude(Degree latitude) {
        this.latitude = validateLatitude(latitude);
    }

    /**
     * Returns the string representation of the latitude for the specified
     * prefix style and unit.
     *
     * @param prefix the prefix style, either {@link #PREFIX_SIGN} or
     *        {@link #PREFIX_DIRECTION}
     * @param unit the unit, one of {@link LocationProvider#UNIT_DEGREE},
     *        {@link LocationProvider#UNIT_DMS}, or
     *        {@link LocationProvider#UNIT_DMS_2}
     * @return the string representation of the latitude
     * @throws IllegalArgumentException if {@code prefix} or {@code unit} is invalid
     */
    public String getLatitudeString(int prefix, int unit) {
        validatePrefix(prefix);
        validateUnit(unit);
        return formatCoordinate(latitude.getFloatingPointNumber(), Axis.LATITUDE, prefix, unit);
    }

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    public Degree getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude.
     *
     * @param longitude the longitude
     * @throws NullPointerException if {@code longitude} is {@code null}
     * @throws IllegalArgumentException if the specified value is outside
     *         {@code [-180 degrees, 180 degrees]}
     */
    public void setLongitude(Degree longitude) {
        this.longitude = normalizeLongitude(validateLongitude(longitude));
    }

    /**
     * Returns the string representation of the longitude for the specified
     * prefix style and unit.
     *
     * @param prefix the prefix style, either {@link #PREFIX_SIGN} or
     *        {@link #PREFIX_DIRECTION}
     * @param unit the unit, one of {@link LocationProvider#UNIT_DEGREE},
     *        {@link LocationProvider#UNIT_DMS}, or
     *        {@link LocationProvider#UNIT_DMS_2}
     * @return the string representation of the longitude
     * @throws IllegalArgumentException if {@code prefix} or {@code unit} is invalid
     */
    public String getLongitudeString(int prefix, int unit) {
        validatePrefix(prefix);
        validateUnit(unit);
        return formatCoordinate(longitude.getFloatingPointNumber(), Axis.LONGITUDE, prefix, unit);
    }

    /**
     * Gets the altitude.
     *
     * @return the altitude in meters, or {@link #ALTITUDE_UNKNOWN}
     */
    public int getAltitude() {
        return altitude;
    }

    /**
     * Sets the altitude.
     *
     * @param altitude the altitude in meters, or {@link #ALTITUDE_UNKNOWN}
     */
    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    /**
     * Gets the horizontal accuracy in meters.
     *
     * @return the horizontal accuracy in meters; {@link #ACCURACY_UNKNOWN} if
     *         the accuracy is unknown
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * Calculates the surface distance between the location represented by this
     * object and the specified destination.
     * Altitude information is ignored.
     * If the datum of {@code dst} differs from the datum of this object, the
     * datum of {@code dst} is converted to the datum of this object first.
     * The algorithm used is spherical trigonometry.
     *
     * @param dst the destination
     * @return the surface distance to the destination, in meters
     * @throws NullPointerException if {@code dst} is {@code null}
     */
    public double calculateDistance(Location dst) {
        Objects.requireNonNull(dst, "dst");
        Location normalized = dst.datum == datum ? dst : dst.transform(datum);

        double lat1 = Math.toRadians(latitude.getFloatingPointNumber());
        double lat2 = Math.toRadians(normalized.latitude.getFloatingPointNumber());
        double lon1 = Math.toRadians(longitude.getFloatingPointNumber());
        double lon2 = Math.toRadians(normalized.longitude.getFloatingPointNumber());

        double centralAngle = Math.acos(clamp(
                Math.sin(lat1) * Math.sin(lat2)
                        + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1),
                -1.0d, 1.0d));
        return datum == LocationProvider.DATUM_TOKYO ? centralAngle * TOKYO_RADIUS_METERS
                : centralAngle * WGS84_RADIUS_METERS;
    }

    /**
     * Calculates the azimuth from the location represented by this object to
     * the specified destination.
     * The angle is measured clockwise from true north.
     * If the datum of {@code dst} differs from the datum of this object, the
     * datum of {@code dst} is converted to the datum of this object first.
     *
     * @param dst the destination
     * @return the azimuth to the destination
     * @throws NullPointerException if {@code dst} is {@code null}
     * @throws ArithmeticException if the azimuth cannot be calculated, such as
     *         when both locations represent the same point
     */
    public Degree calculateAzimuth(Location dst) {
        Objects.requireNonNull(dst, "dst");
        Location normalized = dst.datum == datum ? dst : dst.transform(datum);

        double lat1 = Math.toRadians(latitude.getFloatingPointNumber());
        double lat2 = Math.toRadians(normalized.latitude.getFloatingPointNumber());
        double lon1 = Math.toRadians(longitude.getFloatingPointNumber());
        double lon2 = Math.toRadians(normalized.longitude.getFloatingPointNumber());
        double deltaLon = lon2 - lon1;

        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        if (Math.abs(x) < 1.0e-15d && Math.abs(y) < 1.0e-15d) {
            throw new ArithmeticException("azimuth is undefined for identical points");
        }

        double azimuth = Math.toDegrees(Math.atan2(y, x));
        if (Double.isNaN(azimuth)) {
            throw new ArithmeticException("azimuth is undefined");
        }
        if (azimuth < 0.0d) {
            azimuth += 360.0d;
        }
        return new Degree(azimuth);
    }

    /**
     * Gets the query string for the location-information URL corresponding to
     * this object in i Navi Link (MOPA) format.
     * The separator {@code ?} between the URL and the query string is not
     * included.
     * If the stored accuracy is unknown, the returned string does not contain
     * the {@code X-acc} parameter.
     *
     * @return the query string in i Navi Link (MOPA) format
     * @deprecated In DoJa-4.0 or later, use {@link #getJointRFIURL()}.
     */
    @Deprecated
    public String getPointOfInterestURL() {
        StringBuilder builder = new StringBuilder();
        builder.append("pos=");
        builder.append(getLatitudeString(PREFIX_DIRECTION, LocationProvider.UNIT_DMS));
        builder.append(getLongitudeString(PREFIX_DIRECTION, LocationProvider.UNIT_DMS));
        builder.append("&geo=");
        builder.append(datumToString(datum));
        if (accuracy != ACCURACY_UNKNOWN) {
            builder.append("&X-acc=");
            builder.append(accuracyToLevel(accuracy));
        }
        return builder.toString();
    }

    /**
     * Gets the query string for the location-information URL corresponding to
     * this object in Joint RFI format.
     * The separator {@code ?} between the URL and the query string is not
     * included.
     * The returned query string is encoded using
     * {@code application/x-www-form-urlencoded}.
     * If the stored altitude is unknown, the returned string does not contain
     * the {@code alt} parameter.
     * If the stored accuracy is unknown, the returned string does not contain
     * the {@code x-acc} parameter.
     *
     * @return the query string in Joint RFI format
     */
    public String getJointRFIURL() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("lat", getLatitudeString(PREFIX_SIGN, LocationProvider.UNIT_DEGREE));
        parameters.put("lon", getLongitudeString(PREFIX_SIGN, LocationProvider.UNIT_DEGREE));
        if (altitude != ALTITUDE_UNKNOWN) {
            parameters.put("alt", Integer.toString(altitude));
        }
        parameters.put("geo", datumToString(datum));
        if (accuracy != ACCURACY_UNKNOWN) {
            parameters.put("x-acc", Integer.toString(accuracyToLevel(accuracy)));
        }

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!first) {
                builder.append('&');
            }
            first = false;
            builder.append(URLEncoder.encode(entry.getKey()));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue()));
        }
        return builder.toString();
    }

    private static Degree validateLatitude(Degree latitude) {
        Objects.requireNonNull(latitude, "latitude");
        double value = latitude.getFloatingPointNumber();
        if (Double.isNaN(value) || value < -90.0d || value > 90.0d) {
            throw new IllegalArgumentException("latitude out of range: " + value);
        }
        return latitude;
    }

    private static Degree validateLongitude(Degree longitude) {
        Objects.requireNonNull(longitude, "longitude");
        double value = longitude.getFloatingPointNumber();
        if (Double.isNaN(value) || value < -180.0d || value > 180.0d) {
            throw new IllegalArgumentException("longitude out of range: " + value);
        }
        return longitude;
    }

    private static Degree normalizeLongitude(Degree longitude) {
        double value = longitude.getFloatingPointNumber();
        if (Double.doubleToLongBits(value) == Double.doubleToLongBits(180.0d)) {
            return new Degree(-180.0d);
        }
        return longitude;
    }

    private static void validateDatum(int datum) {
        if (datum != LocationProvider.DATUM_WGS84 && datum != LocationProvider.DATUM_TOKYO) {
            throw new IllegalArgumentException("datum out of range: " + datum);
        }
    }

    private static void validateAccuracy(int accuracy) {
        if (accuracy < ACCURACY_UNKNOWN) {
            throw new IllegalArgumentException("accuracy out of range: " + accuracy);
        }
    }

    private static void validatePrefix(int prefix) {
        if (prefix != PREFIX_DIRECTION && prefix != PREFIX_SIGN) {
            throw new IllegalArgumentException("prefix out of range: " + prefix);
        }
    }

    private static void validateUnit(int unit) {
        if (unit != LocationProvider.UNIT_DEGREE
                && unit != LocationProvider.UNIT_DMS
                && unit != LocationProvider.UNIT_DMS_2) {
            throw new IllegalArgumentException("unit out of range: " + unit);
        }
    }

    private static String formatCoordinate(double value, Axis axis, int prefix, int unit) {
        boolean negative = value < 0.0d;
        double absolute = Math.abs(value);
        if (unit == LocationProvider.UNIT_DEGREE) {
            String numeric = String.format(Locale.US, "%.6f", absolute);
            if (axis == Axis.LONGITUDE && "180.000000".equals(numeric)) {
                negative = true;
            }
            return degreePrefix(axis, prefix, negative, false) + numeric;
        }

        if (unit == LocationProvider.UNIT_DMS) {
            long totalCentiseconds = Math.round(absolute * 360000.0d);
            long degreePart = totalCentiseconds / 360000L;
            long remainder = totalCentiseconds % 360000L;
            long minutePart = remainder / 6000L;
            long secondPart = (remainder % 6000L) / 100L;
            long centisecondPart = remainder % 100L;
            if (axis == Axis.LONGITUDE && degreePart == 180L && minutePart == 0L
                    && secondPart == 0L && centisecondPart == 0L) {
                negative = true;
            }
            return degreePrefix(axis, prefix, negative, false)
                    + degreePart + '.'
                    + twoDigits(minutePart) + '.'
                    + twoDigits(secondPart) + '.'
                    + twoDigits(centisecondPart);
        }

        long totalMilliseconds = Math.round(absolute * 3600000.0d);
        long degreePart = totalMilliseconds / 3600000L;
        long remainder = totalMilliseconds % 3600000L;
        long minutePart = remainder / 60000L;
        long secondWhole = (remainder % 60000L) / 1000L;
        long secondFraction = remainder % 1000L;
        if (axis == Axis.LONGITUDE && degreePart == 180L && minutePart == 0L
                && secondWhole == 0L && secondFraction == 0L) {
            negative = true;
        }
        int width = axis == Axis.LATITUDE ? 2 : 3;
        return degreePrefix(axis, prefix, negative, true)
                + zeroPad(degreePart, width) + '.'
                + twoDigits(minutePart) + '.'
                + twoDigits(secondWhole) + '.'
                + zeroPad(secondFraction, 3);
    }

    private static String degreePrefix(Axis axis, int prefix, boolean negative, boolean explicitPositiveSign) {
        if (prefix == PREFIX_DIRECTION) {
            if (axis == Axis.LATITUDE) {
                return negative ? "S" : "N";
            }
            return negative ? "W" : "E";
        }
        if (negative) {
            return "-";
        }
        return explicitPositiveSign ? "+" : "";
    }

    private static ParsedLocation parseLocationUrl(String url, int defaultAltitude) {
        Objects.requireNonNull(url, "url");
        String query = extractQuery(url);
        Map<String, String> parameters = parseQueryParameters(query);
        if (parameters.containsKey("pos")) {
            return parseMopa(parameters, defaultAltitude);
        }
        return parseJointRfi(parameters, defaultAltitude);
    }

    private static ParsedLocation parseJointRfi(Map<String, String> parameters, int defaultAltitude) {
        String latitudeText = parameters.get("lat");
        String longitudeText = parameters.get("lon");
        String datumText = parameters.get("geo");
        if (latitudeText == null || longitudeText == null || datumText == null) {
            throw new IllegalArgumentException("missing required Joint RFI parameter");
        }

        Degree latitude = parseCoordinateString(latitudeText, Axis.LATITUDE);
        Degree longitude = parseCoordinateString(longitudeText, Axis.LONGITUDE);
        int datum = parseDatum(datumText);
        int altitude = parameters.containsKey("alt") ? parseInteger(parameters.get("alt"), "alt") : defaultAltitude;
        int accuracy = parameters.containsKey("x-acc")
                ? accuracyFromLevel(parameters.get("x-acc"))
                : ACCURACY_UNKNOWN;
        return new ParsedLocation(validateLatitude(latitude), normalizeLongitude(validateLongitude(longitude)),
                altitude, datum, accuracy);
    }

    private static ParsedLocation parseMopa(Map<String, String> parameters, int defaultAltitude) {
        String position = parameters.get("pos");
        String datumText = parameters.get("geo");
        if (position == null || datumText == null) {
            throw new IllegalArgumentException("missing required MOPA parameter");
        }

        Degree latitude;
        Degree longitude;
        if (position.indexOf(',') >= 0) {
            String[] parts = position.split(",", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("invalid pos parameter: " + position);
            }
            latitude = parseCoordinateString(parts[0], Axis.LATITUDE);
            longitude = parseCoordinateString(parts[1], Axis.LONGITUDE);
        } else {
            Matcher matcher = MOPA_COMBINED.matcher(position);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("invalid pos parameter: " + position);
            }
            latitude = parseCoordinateString(matcher.group(1), Axis.LATITUDE);
            longitude = parseCoordinateString(matcher.group(2), Axis.LONGITUDE);
        }

        int datum = parseDatum(datumText);
        int accuracy = parameters.containsKey("X-acc")
                ? accuracyFromLevel(parameters.get("X-acc"))
                : ACCURACY_UNKNOWN;
        return new ParsedLocation(validateLatitude(latitude), normalizeLongitude(validateLongitude(longitude)),
                defaultAltitude, datum, accuracy);
    }

    private static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (query.isEmpty()) {
            return parameters;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0]);
            String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1]) : "";
            parameters.putIfAbsent(key, value);
        }
        return parameters;
    }

    private static String extractQuery(String url) {
        int question = url.indexOf('?');
        String query = question >= 0 ? url.substring(question + 1) : url;
        int fragment = query.indexOf('#');
        if (fragment >= 0) {
            query = query.substring(0, fragment);
        }
        if (query.startsWith("&")) {
            query = query.substring(1);
        }
        if (query.isEmpty()) {
            throw new IllegalArgumentException("empty location query");
        }
        return query;
    }

    private static Degree parseCoordinateString(String text, Axis axis) {
        Objects.requireNonNull(text, axis == Axis.LATITUDE ? "latitude" : "longitude");

        Matcher matcher = DEGREE_COORDINATE.matcher(text);
        if (matcher.matches()) {
            return parseDegreeCoordinate(matcher.group(1), matcher.group(2), matcher.group(3), axis, false);
        }

        matcher = SIGNED_DEGREE_COORDINATE.matcher(text);
        if (matcher.matches()) {
            String sign = matcher.group(1);
            if ("+".equals(sign)) {
                throw new IllegalArgumentException("positive sign is not allowed in UNIT_DEGREE format: " + text);
            }
            return parseDegreeCoordinate(sign, matcher.group(2), matcher.group(3), axis, false);
        }

        matcher = DMS_COORDINATE.matcher(text);
        if (matcher.matches()) {
            return parseDmsCoordinate(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(4), matcher.group(5), axis, false);
        }

        matcher = DMS2_COORDINATE.matcher(text);
        if (matcher.matches()) {
            return parseDmsCoordinate(matcher.group(1), matcher.group(2), matcher.group(3),
                    matcher.group(4), matcher.group(5), axis, true);
        }

        throw new IllegalArgumentException("invalid coordinate format: " + text);
    }

    private static Degree parseDegreeCoordinate(String prefix, String degreesPart, String fractionPart,
                                                Axis axis, boolean plusAllowed) {
        SignAndDirection sign = parseSign(prefix, axis, plusAllowed, false);
        double value = Double.parseDouble(degreesPart + "." + fractionPart);
        value = sign.negative ? -value : value;
        Degree degree = new Degree(value);
        return axis == Axis.LATITUDE ? validateLatitude(degree) : normalizeLongitude(validateLongitude(degree));
    }

    private static Degree parseDmsCoordinate(String prefix, String degreePart, String minutePart,
                                             String secondPart, String fractionPart, Axis axis,
                                             boolean plusRequiredForPositive) {
        SignAndDirection sign = parseSign(prefix, axis, plusRequiredForPositive, true);
        int degree = parseInteger(degreePart, "degree");
        int minute = parseInteger(minutePart, "minute");
        double second = parseInteger(secondPart, "second") + parseInteger(fractionPart, "fraction") / 1000.0d;
        if (!plusRequiredForPositive) {
            second = parseInteger(secondPart, "second") + parseInteger(fractionPart, "fraction") / 100.0d;
        }
        int signedDegree = sign.negative ? -degree : degree;
        Degree result = new Degree(signedDegree, minute, (float) second);
        return axis == Axis.LATITUDE ? validateLatitude(result) : normalizeLongitude(validateLongitude(result));
    }

    private static SignAndDirection parseSign(String prefix, Axis axis, boolean plusRequiredForPositive,
                                              boolean plusAllowed) {
        if (prefix == null || prefix.isEmpty()) {
            if (plusRequiredForPositive) {
                throw new IllegalArgumentException("positive sign is required in UNIT_DMS_2 PREFIX_SIGN format");
            }
            return new SignAndDirection(false);
        }

        if (axis == Axis.LATITUDE) {
            if ("N".equals(prefix)) {
                return new SignAndDirection(false);
            }
            if ("S".equals(prefix)) {
                return new SignAndDirection(true);
            }
        } else {
            if ("E".equals(prefix)) {
                return new SignAndDirection(false);
            }
            if ("W".equals(prefix)) {
                return new SignAndDirection(true);
            }
        }

        if ("-".equals(prefix)) {
            return new SignAndDirection(true);
        }
        if ("+".equals(prefix)) {
            if (!plusAllowed) {
                throw new IllegalArgumentException("unexpected + prefix");
            }
            return new SignAndDirection(false);
        }
        throw new IllegalArgumentException("invalid prefix: " + prefix);
    }

    private static int parseDatum(String datumText) {
        if ("wgs84".equals(datumText)) {
            return LocationProvider.DATUM_WGS84;
        }
        if ("tokyo".equals(datumText)) {
            return LocationProvider.DATUM_TOKYO;
        }
        throw new IllegalArgumentException("invalid datum: " + datumText);
    }

    private static int parseInteger(String text, String parameterName) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid " + parameterName + ": " + text, exception);
        }
    }

    private static int accuracyFromLevel(String levelText) {
        return switch (parseInteger(levelText, "accuracy level")) {
            case 1 -> ACCURACY_COARSE;
            case 2 -> ACCURACY_NORMAL;
            case 3 -> ACCURACY_FINE;
            default -> throw new IllegalArgumentException("invalid accuracy level: " + levelText);
        };
    }

    private static int accuracyToLevel(int accuracy) {
        if (accuracy >= 300) {
            return 1;
        }
        if (accuracy >= 50) {
            return 2;
        }
        return 3;
    }

    private static String datumToString(int datum) {
        return datum == LocationProvider.DATUM_WGS84 ? "wgs84" : "tokyo";
    }

    private static String twoDigits(long value) {
        return zeroPad(value, 2);
    }

    private static String zeroPad(long value, int width) {
        String text = Long.toString(value);
        if (text.length() >= width) {
            return text;
        }
        StringBuilder builder = new StringBuilder(width);
        for (int i = text.length(); i < width; i++) {
            builder.append('0');
        }
        builder.append(text);
        return builder.toString();
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private enum Axis {
        LATITUDE,
        LONGITUDE
    }

    private record ParsedLocation(Degree latitude, Degree longitude, int altitude, int datum, int accuracy) {
    }

    private record SignAndDirection(boolean negative) {
    }
}
