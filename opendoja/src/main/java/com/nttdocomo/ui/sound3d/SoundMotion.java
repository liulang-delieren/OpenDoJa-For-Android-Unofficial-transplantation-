package com.nttdocomo.ui.sound3d;

import com.nttdocomo.ui.Audio3DLocalization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Defines time-varying 3D sound positions.
 */
public class SoundMotion implements Audio3DLocalization {
    private final List<Entry> entries = new ArrayList<>();

    /**
     * Creates an empty motion object.
     */
    public SoundMotion() {
    }

    /**
     * Adds a sound position at the specified time.
     *
     * @param time the time value
     * @param position the position to add
     */
    public void addPosition(int time, SoundPosition position) {
        if (position == null) {
            throw new NullPointerException("position");
        }
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).time == time) {
                entries.set(i, new Entry(time, position));
                return;
            }
        }
        entries.add(new Entry(time, position));
        entries.sort(Comparator.comparingInt(entry -> entry.time));
    }

    /**
     * Returns the number of registered positions.
     *
     * @return the number of positions
     */
    public int size() {
        return entries.size();
    }

    /**
     * Returns the time value at the specified index.
     *
     * @param index the index to query
     * @return the stored time value
     */
    public int getTime(int index) {
        return entries.get(index).time;
    }

    /**
     * Returns the position at the specified index.
     *
     * @param index the index to query
     * @return the stored position
     */
    public SoundPosition getPosition(int index) {
        return entries.get(index).position;
    }

    /**
     * Removes the position at the specified index.
     *
     * @param index the index to remove
     */
    public void removePosition(int index) {
        entries.remove(index);
    }

    private record Entry(int time, SoundPosition position) {
    }
}
