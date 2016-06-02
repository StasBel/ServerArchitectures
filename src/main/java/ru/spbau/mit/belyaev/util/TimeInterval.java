package ru.spbau.mit.belyaev.util;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

public class TimeInterval {
    private long startTime;
    private long stopTime;

    // NOT thread-safe

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        stopTime = System.currentTimeMillis();
    }

    public long getTimeAbs() {
        return stopTime - startTime;
    }
}
