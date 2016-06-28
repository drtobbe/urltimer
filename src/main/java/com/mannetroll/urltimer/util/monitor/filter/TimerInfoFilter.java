package com.mannetroll.urltimer.util.monitor.filter;

import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
public class TimerInfoFilter extends AbstractTimerInfoFilter {

    /**
     * Default constructor
     */
    public TimerInfoFilter() {
        super(TimerInfoStats.getInstance(), "URI - Timimg");
    }
}