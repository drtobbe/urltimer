package com.mannetroll.urltimer.util.monitor.statistics;

/**
 * @author drtobbe
 */
public class TimerInfoStats extends AbstractTimerInfoStats {
    private static final long serialVersionUID = 1L;
    private static TimerInfoStats instance = null;

    private TimerInfoStats() {
        super();
    }

    /**
     * @return
     */
    public static TimerInfoStats getInstance() {
        if (instance == null) {
            instance = create();
        }
        return instance;
    }

    private static synchronized TimerInfoStats create() {
        if (instance == null) {
            instance = new TimerInfoStats();
            instance.setName("UrlTimer");
        }
        return instance;
    }
}
