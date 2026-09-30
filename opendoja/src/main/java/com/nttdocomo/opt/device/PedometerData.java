package com.nttdocomo.opt.device;

import java.util.Date;

/**
 * Represents one day's pedometer information.
 */
public class PedometerData {
    private final Date date;
    private final int numberOfSteps;
    private final int distance;
    private final int numberOfAerobicsSteps;
    private final int aerobicsTime;
    private final int consumptionCalorie;
    private final int amountOfCombustionFat;

    /**
     * Applications cannot create instances of this class directly.
     */
    protected PedometerData() {
        this(new Date(0L), 0, 0, 0, 0, 0, 0);
    }

    PedometerData(Date date,
                  int numberOfSteps,
                  int distance,
                  int numberOfAerobicsSteps,
                  int aerobicsTime,
                  int consumptionCalorie,
                  int amountOfCombustionFat) {
        this.date = date == null ? new Date(0L) : new Date(date.getTime());
        this.numberOfSteps = numberOfSteps;
        this.distance = distance;
        this.numberOfAerobicsSteps = numberOfAerobicsSteps;
        this.aerobicsTime = aerobicsTime;
        this.consumptionCalorie = consumptionCalorie;
        this.amountOfCombustionFat = amountOfCombustionFat;
    }

    /**
     * Gets a {@link Date} object for the date held by this object.
     * The time information of the returned object is 00:00:00.
     *
     * @return the date object
     */
    public Date getDate() {
        return new Date(date.getTime());
    }

    /**
     * Gets the step count for the day.
     *
     * @return the step count
     */
    public int getNumberOfSteps() {
        return numberOfSteps;
    }

    /**
     * Gets the walking distance for the day.
     *
     * @return the walking distance, converted to meters
     */
    public int getDistance() {
        return distance;
    }

    /**
     * Gets the number of steps counted as aerobic exercise for the day.
     *
     * @return the number of aerobic steps
     */
    public int getNumberOfAerobicsSteps() {
        return numberOfAerobicsSteps;
    }

    /**
     * Gets the accumulated time spent in aerobic exercise for the day.
     *
     * @return the aerobic-exercise time, converted to minutes
     */
    public int getAerobicsTime() {
        return aerobicsTime;
    }

    /**
     * Gets the calories consumed by walking for the day.
     *
     * @return the consumed calories, converted to kilocalories
     */
    public int getConsumptionCalorie() {
        return consumptionCalorie;
    }

    /**
     * Gets the fat burned by walking for the day.
     *
     * @return the burned fat amount, converted to grams
     */
    public int getAmountOfCombustionFat() {
        return amountOfCombustionFat;
    }
}
