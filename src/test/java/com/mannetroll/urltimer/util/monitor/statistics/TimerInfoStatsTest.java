package com.mannetroll.urltimer.util.monitor.statistics;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
public class TimerInfoStatsTest {
    private final static AbstractTimerInfoStats statistics = TimerInfoStats.getInstance();
    private final static Random randomGenerator = new Random();

    @Test
    public void testStatistic() {
        long responsetime_ms = 500;
        int chunk = 1024;
        String key = "START";
        //
        long now1 = System.currentTimeMillis();
        statistics.addCall(key, responsetime_ms, chunk, now1);
        statistics.addTotalTime(responsetime_ms, chunk, now1);
        //
        long now2 = System.currentTimeMillis();
        statistics.addCall(key, 2 * responsetime_ms, 2 * chunk, now2);
        statistics.addTotalTime(2 * responsetime_ms, 2 * chunk, now2);
        //
        double mt1 = statistics.getAverage();
        double mt2 = statistics.getStatData().get(key).getMeanTime();
        Assert.assertEquals(mt1, mt2, Double.MIN_VALUE);
        //
        double st1 = statistics.getStandardDeviation();
        double st2 = statistics.getStatData().get(key).getTimeStandardDeviation();
        Assert.assertEquals(st1, st2, Double.MIN_VALUE);
        //
        double mc1 = statistics.getChunkAverage();
        double mc2 = statistics.getStatData().get(key).getMeanBytes();
        Assert.assertEquals(mc1, mc2, Double.MIN_VALUE);
        //
        double mr1 = statistics.getRatesAverage();
        double mr2 = statistics.getStatData().get(key).getMeanRate();
        Assert.assertEquals(mr1, mr2, Double.MIN_VALUE);
    }

    @Test
    public void testPlot() throws Exception {
        for (int i = 0; i < 10000; i++) {
            fakeCall("GET|200|FAKE3");
        }
        statistics.getResponseTimes();
        statistics.getRatesSizes();
        statistics.getChunkSizes();
    }

    private static void fakeCall(String key) {
        long responsetime_ms = Math.round(getGaussian(2000, 2));
        int chunk = (int) Math.round(getGaussian(15 * 1024, 2));
        long now = System.currentTimeMillis();
        statistics.addCall(key, responsetime_ms, chunk, now);
        statistics.addTotalTime(responsetime_ms, chunk, now);
    }

    private static double getGaussian(double a, double b) {
        return Math.round(Math.max(0D, a * (1.0D + randomGenerator.nextGaussian() / b)));
    }

}
