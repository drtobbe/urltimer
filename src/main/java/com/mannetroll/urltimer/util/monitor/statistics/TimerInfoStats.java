package com.mannetroll.urltimer.util.monitor.statistics;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author drtobbe
 */
public class TimerInfoStats extends AbstractTimerInfoStats {
    private static final long serialVersionUID = 1L;
    private final static Map<String, TimerInfoStats> hosts = new Hashtable<String, TimerInfoStats>();
    private final String host;

    private TimerInfoStats(String host) {
        super();
        this.host = host;
        setName(host);
    }

    public String getHost() {
        return host;
    }

    /**
     * @return
     */
    public static synchronized TimerInfoStats getInstance(String host) {
        TimerInfoStats instance = hosts.get(host);
        if (instance == null) {
            instance = new TimerInfoStats(host);
            hosts.put(host, instance);
        }
        return instance;
    }
}
