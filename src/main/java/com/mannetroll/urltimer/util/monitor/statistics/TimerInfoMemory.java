package com.mannetroll.urltimer.util.monitor.statistics;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author drtobbe
 */
public class TimerInfoMemory implements Serializable {
    private static final long serialVersionUID = 1L;
    private long memStart = Runtime.getRuntime().totalMemory();
    private long memMax = Runtime.getRuntime().maxMemory();
    private long free = 1;
    private long total = 0;
    private final long TIME_INTERVAL = 1000; // 1000 ms = 1 s
    private final long start = System.currentTimeMillis() / TIME_INTERVAL;
    private long last = start;
    private final int CHART = 185; //CHART height is 190 pixels
    private static final int TIME_SIZE = 590; //CHART width is 600 pixels
    private static final long[] values = new long[TIME_SIZE];

    public TimerInfoMemory() {
    }

    /**
     * 
     */
    public void clear() {
        synchronized (values) {
            Arrays.fill(values, 0L);
        }
    }

    /**
     *
     */
    public synchronized void storeMemory() {
        // Free Memory
        free = Runtime.getRuntime().freeMemory();

        // Total Allocated Memory
        total = Runtime.getRuntime().totalMemory();

        // Fraction 0-100%
        double fraction = ((1d - ((double) free / (double) total)));

        // Number of intervals
        long now = (System.currentTimeMillis() / TIME_INTERVAL) - start;

        //draw memory line
        for (long i = last; i <= now; i++) {
            int index = ((int) i) % TIME_SIZE;
            values[index] = Math.round(CHART * fraction);
        }

        // Save current time
        last = now;
    }

    /**
     * @return total
     */
    public long getTotal() {
        return total;
    }

    /**
     * @return memMax
     */
    public long getMemMax() {
        return memMax;
    }

    /**
     * @return free
     */
    public long getFree() {
        return free;
    }

    /**
     * @return memStart
     */
    public long getMemStart() {
        return memStart;
    }

    /**
     * @return values
     */
    public long[] getValues() {
        synchronized (values) {
            return values;
        }
    }
}