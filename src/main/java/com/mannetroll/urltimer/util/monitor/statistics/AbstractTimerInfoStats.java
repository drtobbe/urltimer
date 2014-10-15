package com.mannetroll.urltimer.util.monitor.statistics;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author drtobbe
 */
@SuppressWarnings("unchecked")
public abstract class AbstractTimerInfoStats implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTimerInfoStats.class);
    private static final String TOTAL = "TOTAL";
    private static final long serialVersionUID = 1L;
    private final long serverStart = System.currentTimeMillis();
    private long createTime = 0;
    private long lastTime = 0;
    private HashMap<String, TimerInfoItem> base = new HashMap<String, TimerInfoItem>();
    private Map<String, TimerInfoItem> calls = Collections.synchronizedMap(base);
    private boolean addQueryToKey = false;
    private long notModified = 0;
    private long etagCached = 0;
    private long rendered = 0;
    private long ok200 = 0;
    private long err50X = 0;
    private long err40X = 0;
    private long err30X = 0;
    private int pp = 95;
    private String name;
    private static TimerInfoMemory memoryInfo = new TimerInfoMemory();

    public AbstractTimerInfoStats() {
        calls.put(TOTAL, new TimerInfoItem(TOTAL));
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    public void setAddQueryToKey(boolean b) {
        addQueryToKey = b;
    }

    public boolean isAddQueryToKey() {
        return addQueryToKey;
    }

    /**
     * @param timeSlice
     */
    public void addTotalTime(double timeSlice, int chunk, long now) {
        addCallLocal(TOTAL, timeSlice, chunk, now);
        storeResponseTime(timeSlice);
        storeChunkSize(chunk);
        storeResponseRate(((double) chunk) / (timeSlice / 1000D));
        memoryInfo.storeMemory();
    }

    /**
     * @return TOTALNofCalls
     */
    public long getTotalNofCalls() {
        TimerInfoItem sd = (TimerInfoItem) calls.get(TOTAL);
        long retval = 1L;
        if (sd != null) {
            retval = sd.getNofCalls();
        }
        return retval;
    }

    /**
     * @return TOTALTotalTime
     */
    public double getTotalTotalTime() {
        TimerInfoItem sd = (TimerInfoItem) calls.get(TOTAL);
        double retval = 1.0D;
        if (sd != null) {
            retval = sd.getTotalTime();
        }
        return retval;
    }

    /**
     * @return TOTALTotalTime
     */
    public double getTotalAverage() {
        TimerInfoItem sd = (TimerInfoItem) calls.get(TOTAL);
        double retval = 0.0D;
        if (sd != null) {
            retval = sd.getMeanTime();
        }
        return retval;
    }

    public double getPaylodAverage() {
        TimerInfoItem sd = (TimerInfoItem) calls.get(TOTAL);
        double retval = 0.0D;
        if (sd != null) {
            retval = sd.getMeanBytes();
        }
        return retval;
    }

    public double getBitrateAverage() {
        TimerInfoItem sd = (TimerInfoItem) calls.get(TOTAL);
        double retval = 0.0D;
        if (sd != null) {
            retval = sd.getMeanRate();
        }
        return retval;
    }

    public void addCall(String key, double timeSlice, long now) {
        addCallLocal(key, timeSlice, 0, now);
    }

    public void addCall(String key, double timeSlice, int chunk, long now) {
        addCallLocal(key, timeSlice, chunk, now);
    }

    private synchronized void addCallLocal(String key, double timeSlice, int chunk, long now) {
        if (createTime == 0) {
            createTime = now;
        }
        lastTime = now;
        TimerInfoItem item = (TimerInfoItem) calls.get(key);
        if (item == null) {
            item = new TimerInfoItem(key);
            calls.put(key, item);
        }
        item.addCall(timeSlice, chunk, now);
    }

    /**
     * clear
     */
    public synchronized void clear() {
        memoryInfo.clear();
        calls.clear();
        base = new HashMap<String, TimerInfoItem>();
        calls = Collections.synchronizedMap(base);
        calls.put(TOTAL, new TimerInfoItem(TOTAL));
        createTime = 0;
        lastTime = createTime;
        rtimes = new int[RESP_SIZE];
        chunks = new int[CHUNK_SIZE];
        rates = new int[RATE_SIZE];
        notModified = 0;
        etagCached = 0;
        rendered = 0;
        ok200 = 0;
        err50X = 0;
        err40X = 0;
        err30X = 0;
    }

    public long getServerStart() {
        return serverStart;
    }

    /**
     * @return createTime
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * @return lastTime
     */
    public long getLastTime() {
        return lastTime;
    }

    public long getElapsedTime() {
        return lastTime - createTime;
    }

    /**
     * @param key
     * @return TotalNofCalls
     */
    public long getTotalNofCalls(String key) {
        TimerInfoItem sd = (TimerInfoItem) calls.get(key);
        long retval = 0L;
        if (sd != null) {
            retval = sd.getNofCalls();
        }
        return retval;
    }

    /**
     * @param key
     * @return TotalTotalTime
     */
    public double getTotalTotalTime(String key) {
        TimerInfoItem sd = (TimerInfoItem) calls.get(key);
        double retval = 0.0D;
        if (sd != null) {
            retval = sd.getTotalTime();
        }
        return retval;
    }

    /**
     * @return clone
     */
    public synchronized Map<String, TimerInfoItem> getStatData() {
        return (Map<String, TimerInfoItem>) base.clone();
    }

    /**
     * @return size
     */
    public synchronized int getStatDataSize() {
        return calls.size();
    }

    /**
     * @return
     */
    public TimerInfoMemory getMemoryInfo() {
        return memoryInfo;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final int CHART = 185; // CHART height is 190 pixels
    private final int TIME_SIZE = 595; // CHART width is 600 pixels
    private final int RESP_SIZE = 200 * 1000; // collect values upto 200 secs, 800KB
    private final int RESP_SIZEM1 = RESP_SIZE - 1;
    private int[] rtimes = new int[RESP_SIZE];

    /**
     * @param elapsedIn
     */
    public synchronized void storeResponseTime(double elapsedIn) {
        rtimes[Math.min(Math.max((int) elapsedIn, 0), RESP_SIZEM1)]++;
    }

    /**
     * @return
     */
    public int[] getRtimes() {
        return rtimes;
    }

    /**
     * @return rtimes
     */
    public long[] getResponseTimes() {
        logger.info("--");
        double mean = getStatData().get(TOTAL).getMeanTime();
        logger.info("times: " + mean);
        double std = getStatData().get(TOTAL).getTimeStandardDeviation();
        logger.info("std: " + std);
        return getNormalizedArray(rtimes, pp);
    }

    public double getAverage() {
        return getArrayAverage(rtimes);
    }

    public double getStandardDeviation() {
        return getArrayStandardDeviation(rtimes);
    }

    public double getAverageM1() {
        return getArrayAverageM1(rtimes);
    }

    public int getMedian() {
        return getArrayMedian(rtimes);
    }

    public long getSumM1() {
        return getArraySumM1(rtimes);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final int CHUNK_SIZE = 40 * 1024 * 1024; // collect values upto 40 MB, takes 160MB RAM
    private final int CHUNK_SIZEM1 = CHUNK_SIZE - 1;
    private int[] chunks = new int[CHUNK_SIZE];

    /**
     * @param chunk
     */
    public synchronized void storeChunkSize(int chunk) {
        chunks[Math.min(Math.max(chunk, 0), CHUNK_SIZEM1)]++;
    }

    /**
     * @return
     */
    public int[] getChunks() {
        return chunks;
    }

    /**
     * @return rtimes
     */
    public long[] getChunkSizes() {
        logger.info("--");
        double mean = getStatData().get(TOTAL).getMeanBytes();
        logger.info("bytes: " + mean);
        double std = getStatData().get(TOTAL).getBytesStandardDeviation();
        logger.info("std: " + std);
        return getNormalizedArray(chunks, pp);
    }

    /**
     * @return
     */
    public double getChunkAverage() {
        return getArrayAverage(chunks);
    }

    public double getChunkStandardDeviation() {
        return getArrayStandardDeviation(chunks);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final int RATE_SIZE = 50 * 1024 * 1024; // collect values upto 50 MB/s, takes 200MB RAM
    private final int RATE_SIZEM1 = RATE_SIZE - 1;
    private int[] rates = new int[RATE_SIZE];

    /**
     * @param elapsedIn
     */
    public synchronized void storeResponseRate(double rate) {
        rates[Math.min(Math.max((int) rate, 0), RATE_SIZEM1)]++;
    }

    /**
     * @return
     */
    public int[] getRates() {
        return rates;
    }

    /**
     * @return rtimes
     */
    public long[] getRatesSizes() {
        double mean = getStatData().get(TOTAL).getMeanRate();
        logger.info("--");
        logger.info("rates: " + mean);
        double std = getStatData().get(TOTAL).getRateStandardDeviation();
        logger.info("std: " + std);
        return getNormalizedArray(rates, pp);
    }

    public double getRatesAverage() {
        return getArrayAverage(rates);
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private long[] getNormalizedArray(int[] array, int percent) {
        int width = getArrayPercent(array, percent);
        logger.info("width: " + width);
        int compress = Math.max(1, Math.round(width / TIME_SIZE));
        logger.info("compress1: " + compress);
        int[] compressed = compressArray(array, compress);
        //
        // Shift to the median
        int shift = Math.max(0, (int) getArrayMedianM1(compressed) - TIME_SIZE / 2);
        shift = Math.min(shift, compressed.length - TIME_SIZE - 1);
        shift = 0;
        logger.info("shift: " + shift);
        long ymax = getArrayMax(compressed);
        long[] normed = new long[TIME_SIZE];
        for (int i = 0; i < TIME_SIZE; i++) {
            normed[i] = (CHART * compressed[i + shift]) / ymax;
        }
        return normed;
    }

    private int[] compressArray(int[] array, int compress) {
        int newlen = array.length / compress;
        newlen = Math.max(newlen, TIME_SIZE + 1);
        logger.info("newlen: " + newlen);
        compress = array.length / newlen;
        logger.info("compress2: " + compress);
        int[] compressed = new int[newlen];
        for (int i = 0; i < newlen; i++) {
            for (int j = 0; j < compress; j++) {
                compressed[i] += array[i * compress + j];
            }
        }
        return compressed;
    }

    private long getArrayMax(int[] array) {
        // Find ymax
        long ymax = 1;
        for (int i = 1; i < array.length - 1; i++) {
            ymax = Math.max(ymax, array[i]);
        }
        return ymax;
    }

    private double getArrayAverage(int[] array) {
        double sum = 0;
        double first = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
            first += ((double) i) * array[i];
        }
        return (sum > 0 ? first / sum : 0);
    }

    private double getArrayStandardDeviation(int[] array) {
        double sum = 0;
        double first = 0;
        double second = 0;
        //http://en.wikipedia.org/wiki/Variance
        for (int i = 0; i < array.length; ++i) {
            sum += array[i];
            first += ((double) i) * array[i];
            second += ((double) i) * ((double) i) * array[i];
        }
        double mean = (sum > 0 ? first / sum : 0);
        double variance = (sum > 0 ? second / sum - mean * mean : 0);
        return Math.sqrt(variance);
    }

    /*
      double t1 = totalTime2 / nofCalls;
      double t2 = totalTime / nofCalls;
      return Math.sqrt(Math.max(0.0D, t1 - t2 * t2));
     */

    private double getArrayAverageM1(int[] array) {
        double xmean = 0;
        double num = Double.MIN_VALUE;
        for (int i = 1; i < array.length - 1; i++) {
            xmean += i * array[i];
            num += array[i];
        }
        return (num > 0 ? xmean / num : 0);
    }

    private int getArrayMedian(int[] array) {
        long sum = getArraySum(array);
        // find median
        long median = 0;
        for (int i = 0; i < array.length; i++) {
            if (median >= (sum / 2)) {
                return i;
            }
            median += array[i];
        }
        return 0;
    }

    private int getArrayPercent(int[] array, int percent) {
        percent = Math.abs(percent);
        if (percent > 100) {
            percent = 100;
        }
        long sum = getArraySum(array);
        // find median
        long agg = 0;
        double limit = sum * (percent / 100D);
        for (int i = 0; i < array.length; i++) {
            if (agg >= limit) {
                return i;
            }
            agg += array[i];
        }
        return 0;
    }

    private long getArraySum(int[] array) {
        // find sum of hits
        long sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum;
    }

    private int getArrayMedianM1(int[] array) {
        long sum = getArraySumM1(array);
        // find median
        long median = 0;
        for (int i = 1; i < array.length - 1; i++) {
            if (median >= (sum / 2)) {
                return i;
            }
            median += array[i];
        }
        return 0;
    }

    private long getArraySumM1(int[] array) {
        // find sum of hits except boundaries
        long sum = 0;
        for (int i = 1; i < array.length - 1; i++) {
            sum += array[i];
        }
        return sum;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////    

    public void addNotModified() {
        notModified++;
    }

    public long getNotModified() {
        return notModified;
    }

    public void addEtagCached() {
        etagCached++;
    }

    public long getEtagCached() {
        return etagCached;
    }

    public void addRendered() {
        rendered++;
    }

    public long getRendered() {
        return rendered;
    }

    public void add200OK() {
        ok200++;
    }

    public long get200OK() {
        return ok200;
    }

    public void add50X() {
        err50X++;
    }

    public long get50X() {
        return err50X;
    }

    public void add40X() {
        err40X++;
    }

    public long get40X() {
        return err40X;
    }

    public void add30X() {
        err30X++;
    }

    public long get30X() {
        return err30X;
    }

    public void addStatusCode(String response) {
        if ("200".equalsIgnoreCase(response)) {
            add200OK();
        } else if (response != null && response.startsWith("50")) {
            add50X();
        } else if (response != null && response.startsWith("40")) {
            add40X();
        } else if (response != null && response.startsWith("30")) {
            add30X();
        }
    }

}
