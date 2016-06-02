package ru.spbau.mit.belyaev.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by belaevstanislav on 03.06.16.
 * SPBAU Java practice.
 */

public class Stat {
    private final AtomicInteger count;
    private final AtomicLong allTime;

    public Stat() {
        count = new AtomicInteger(0);
        allTime = new AtomicLong(0);
    }

    public void add(long time) {
        count.addAndGet(1);
        allTime.addAndGet(time);
    }

    public void add(TimeInterval timeInterval) {
        count.addAndGet(1);
        allTime.addAndGet(timeInterval.getTimeAbs());
    }

    public long calcAverage() {
        return allTime.get() / count.get();
    }
}
