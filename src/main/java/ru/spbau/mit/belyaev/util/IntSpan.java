package ru.spbau.mit.belyaev.util;

/**
 * Created by belaevstanislav on 03.06.16.
 * SPBAU Java practice.
 */

public class IntSpan {
    private final int to;
    private final int step;
    private int cur;
    private boolean firstTime;

    public IntSpan(int from, int to, int step) {
        this.to = to;
        this.step = step;
        cur = from - step;
        firstTime = true;
    }

    public IntSpan(int point) {
        this(point, point, 0);
    }

    public int next() {
        firstTime = false;
        return (cur += step);
    }

    public boolean hasNext() {
        final boolean result = (step != 0 && cur + step < to) || (firstTime);
        firstTime = false;
        return result;
    }

    public boolean isChange() {
        return step != 0;
    }
}
