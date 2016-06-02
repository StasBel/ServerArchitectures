package ru.spbau.mit.belyaev.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

public class TimeIntervals {
    private final Object lock;
    private final List<TimeInterval> intervals;

    // thread-safe

    public TimeIntervals() {
        lock = new Object();
        intervals = new LinkedList<>();
    }

    public void addInterval(TimeInterval interval) {
        synchronized (lock) {
            intervals.add(interval);
        }
    }
}
