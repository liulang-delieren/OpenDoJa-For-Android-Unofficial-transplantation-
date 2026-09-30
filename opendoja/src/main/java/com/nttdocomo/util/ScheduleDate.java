package com.nttdocomo.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class for specifying a schedule time.
 * This class is used for scheduler settings and i-appli automatic-start
 * settings and holds the contents of a schedule time or automatic-start time.
 * The following date/time specification types can be set: one-time, daily,
 * weekly, monthly, and yearly.
 *
 * <p>For monthly and yearly schedules, behavior for impossible dates such as
 * the 31st in a short month or leap-year handling is device-dependent. Except
 * when setting {@link Calendar#DAY_OF_WEEK}, the behavior of
 * {@link #set(int, int)} and {@link #get(int)} matches the methods of the same
 * name in {@link Calendar}, including out-of-range values. This object does
 * not hold time-zone information.</p>
 *
 * <p>Introduced in DoJa-3.0 (505i).</p>
 *
 * @see com.nttdocomo.system.Schedule
 * @see com.nttdocomo.ui.IApplication
 */
public class ScheduleDate {
    /**
     * Date/time specification type that represents a one-time schedule (=0x01).
     */
    public static final int ONETIME = 1;
    /**
     * Date/time specification type that represents a daily schedule (=0x02).
     */
    public static final int DAILY = 2;
    /**
     * Date/time specification type that represents a weekly schedule (=0x04).
     */
    public static final int WEEKLY = 4;
    /**
     * Date/time specification type that represents a monthly schedule (=0x08).
     */
    public static final int MONTHLY = 8;
    /**
     * Date/time specification type that represents a yearly schedule (=0x10).
     */
    public static final int YEARLY = 16;

    private final int type;
    private final Calendar calendar;

    /**
     * Creates an object with the specified date/time specification type.
     * The date and time values are initialized from the current date and time
     * using the default time zone.
     *
     * @param type the date/time specification type; one of
     *             {@link #ONETIME}, {@link #DAILY}, {@link #WEEKLY},
     *             {@link #MONTHLY}, or {@link #YEARLY}
     * @throws IllegalArgumentException if {@code type} is invalid
     */
    public ScheduleDate(int type) {
        this(type, TimeZone.getDefault());
    }

    /**
     * Creates an object using the specified time zone.
     * The date and time values are initialized from the current time and the
     * specified time zone.
     * If {@code timeZone} is {@code null}, this constructor behaves the same
     * as {@link #ScheduleDate(int)}.
     *
     * @param type the date/time specification type; one of
     *             {@link #ONETIME}, {@link #DAILY}, {@link #WEEKLY},
     *             {@link #MONTHLY}, or {@link #YEARLY}
     * @param timeZone the time zone to use
     * @throws IllegalArgumentException if {@code type} is invalid
     */
    public ScheduleDate(int type, TimeZone timeZone) {
        validateType(type);
        this.type = type;
        this.calendar = Calendar.getInstance(timeZone == null ? TimeZone.getDefault() : timeZone);
    }

    /**
     * Gets the date/time specification type of this schedule time.
     *
     * @return the date/time specification type
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the date/time value of this schedule time.
     * For the values that can be specified for {@code field} and the values
     * that are obtained, refer to {@link #set(int, int)}.
     *
     * @param field the element to obtain
     * @return the configured value
     * @throws IllegalArgumentException if {@code field} is invalid
     */
    public int get(int field) {
        validateField(type, field);
        return calendar.get(field);
    }

    /**
     * Sets the date and time of this schedule time.
     * When the type is {@link #ONETIME}, {@code field} can be
     * {@link Calendar#YEAR}, {@link Calendar#MONTH}, {@link Calendar#DATE},
     * {@link Calendar#HOUR_OF_DAY}, or {@link Calendar#MINUTE}.
     * When the type is {@link #DAILY}, it can be
     * {@link Calendar#HOUR_OF_DAY} or {@link Calendar#MINUTE}.
     * When the type is {@link #WEEKLY}, it can be
     * {@link Calendar#DAY_OF_WEEK}, {@link Calendar#HOUR_OF_DAY}, or
     * {@link Calendar#MINUTE}.
     * When the type is {@link #MONTHLY}, it can be
     * {@link Calendar#DATE}, {@link Calendar#HOUR_OF_DAY}, or
     * {@link Calendar#MINUTE}.
     * When the type is {@link #YEARLY}, it can be
     * {@link Calendar#MONTH}, {@link Calendar#DATE},
     * {@link Calendar#HOUR_OF_DAY}, or {@link Calendar#MINUTE}.
     *
     * @param field the element to set
     * @param value the value to set
     * @throws IllegalArgumentException if {@code field} is invalid, or if
     *         {@code field} is {@link Calendar#DAY_OF_WEEK} and {@code value}
     *         is not one of the weekday constants defined by {@link Calendar}
     */
    public void set(int field, int value) {
        validateField(type, field);
        if (field == Calendar.DAY_OF_WEEK
                && (value < Calendar.SUNDAY || value > Calendar.SATURDAY)) {
            throw new IllegalArgumentException("value");
        }
        calendar.set(field, value);
    }

    private static void validateType(int type) {
        if (type != ONETIME && type != DAILY && type != WEEKLY
                && type != MONTHLY && type != YEARLY) {
            throw new IllegalArgumentException("type");
        }
    }

    private static void validateField(int type, int field) {
        boolean valid = switch (type) {
            case ONETIME -> field == Calendar.YEAR
                    || field == Calendar.MONTH
                    || field == Calendar.DATE
                    || field == Calendar.HOUR_OF_DAY
                    || field == Calendar.MINUTE;
            case DAILY -> field == Calendar.HOUR_OF_DAY
                    || field == Calendar.MINUTE;
            case WEEKLY -> field == Calendar.DAY_OF_WEEK
                    || field == Calendar.HOUR_OF_DAY
                    || field == Calendar.MINUTE;
            case MONTHLY -> field == Calendar.DATE
                    || field == Calendar.HOUR_OF_DAY
                    || field == Calendar.MINUTE;
            case YEARLY -> field == Calendar.MONTH
                    || field == Calendar.DATE
                    || field == Calendar.HOUR_OF_DAY
                    || field == Calendar.MINUTE;
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("field");
        }
    }
}
