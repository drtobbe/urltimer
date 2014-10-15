package com.mannetroll.urltimer.util.monitor.statistics;

/**
 * @author drtobbe
 */
public class TimerInfoItem {
    private long nofCalls = 0;
    private double minTime = Double.MAX_VALUE;
    private double maxTime = 0;
    private double totalTime = 0;
    private double totalTime2 = 0;
    private long frequency = 0;
    private double frequency2 = 0;
    private long minFrequency = Integer.MAX_VALUE;
    private long maxFrequency = 0;
    private long totalBytes = 0;
    private double totalBytes2 = 0;
    private long minBytes = Integer.MAX_VALUE;
    private long maxBytes = 0;
    private double totalRate = 0;
    private double totalRate2 = 0;
    private double minRate = Double.MAX_VALUE;
    private double maxRate = 0;
    private String key = null;
    private long lastTime = System.currentTimeMillis();

    protected TimerInfoItem(String key) {
        this.key = key;
    }

    protected void addCall(double timeSlice, long chunk, long now) {
        totalTime += timeSlice;
        totalTime2 += timeSlice * timeSlice;
        minTime = Math.min(minTime, timeSlice);
        maxTime = Math.max(maxTime, timeSlice);
        //frequency
        long diff = now - this.lastTime;
        if (diff > 0) {
            frequency += diff;
            frequency2 += frequency * frequency;
            minFrequency = Math.min(minFrequency, diff);
            maxFrequency = Math.max(maxFrequency, diff);
        }
        if (chunk > 0) {
            totalBytes += chunk;
            totalBytes2 += chunk * chunk;
            minBytes = Math.min(minBytes, chunk);
            maxBytes = Math.max(maxBytes, chunk);
            double rate = (timeSlice > 0 ? chunk / (timeSlice / 1000) : 0D);
            totalRate += rate;
            totalRate2 += rate * rate;
            minRate = Math.min(minRate, rate);
            maxRate = Math.max(maxRate, rate);
        }
        this.lastTime = now;
        nofCalls++;
    }

    /**
     * @return 
     */
    public double getMeanBytes() {
        return nofCalls != 0 ? ((double) totalBytes) / nofCalls : 0.0D;
    }

    /**
     * @return
     */
    public double getBytesStandardDeviation() {
        if (nofCalls == 0) {
            return 0.0D;
        } else {
            double t1 = totalBytes2 / nofCalls;
            double t2 = totalBytes / nofCalls;
            return Math.sqrt(Math.max(0.0D, t1 - t2 * t2));
        }
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getMinBytes() {
        return minBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    /**
     * @return 
     */
    public double getMeanRate() {
        return nofCalls > 0 ? ((double) totalRate) / nofCalls : 0.0D;
    }

    /**
     * @return
     */
    public double getRateStandardDeviation() {
        if (nofCalls == 0) {
            return 0.0D;
        } else {
            double t1 = totalRate2 / nofCalls;
            double t2 = totalRate / nofCalls;
            return Math.sqrt(Math.max(0.0D, t1 - t2 * t2));
        }
    }

    public double getMinRate() {
        return minRate;
    }

    public double getMaxRate() {
        return maxRate;
    }

    /**
     * @return actually the inverse of frequency 
     */
    public double getMeanFrequency() {
        return nofCalls != 0 ? ((double) frequency) / nofCalls : 0.0D;
    }

    public double getFrequencyStandardDeviation() {
        if (nofCalls == 0) {
            return 0.0D;
        } else {
            double t1 = frequency2 / nofCalls;
            double t2 = frequency / nofCalls;
            return Math.sqrt(Math.max(0.0D, t1 - t2 * t2));
        }
    }

    public long getMinFrequency() {
        return minFrequency;
    }

    public long getMaxFrequency() {
        return maxFrequency;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public double getMeanTime() {
        return nofCalls != 0 ? totalTime / nofCalls : 0.0D;
    }

    public double getTimeStandardDeviation() {
        if (nofCalls == 0) {
            return 0.0D;
        } else {
            double t1 = totalTime2 / nofCalls;
            double t2 = totalTime / nofCalls;
            return Math.sqrt(Math.max(0.0D, t1 - t2 * t2));
        }
    }

    public long getNofCalls() {
        return nofCalls;
    }

    public double getMinTime() {
        return minTime;
    }

    public double getMaxTime() {
        return maxTime;
    }

    public String getKey() {
        return key;
    }

    /**
     * @param totalTotalTime
     * @param threadload 
     * @param elapsedtime 
     * @return
     */
    public double[] getTimeDataArray(double totalTotalTime, double threadload, float elapsedtime) {
        double[] result = new double[14];
        result[0] = nofCalls;
        result[1] = totalTime / 1000;
        result[2] = minTime;
        result[3] = getMeanTime();
        result[4] = maxTime;
        result[5] = getTimeStandardDeviation();
        result[6] = (getMeanTime() > 0 ? 1000D * Math.max(1D, threadload) / getMeanTime() : 0D);
        result[7] = 100D * (totalTime / Math.max(totalTotalTime, Double.MIN_VALUE));
        result[8] = (elapsedtime > 0 ? nofCalls / Math.max(elapsedtime, Double.MIN_VALUE) : 0D);
        result[9] = (getMeanTime() + 3D * getTimeStandardDeviation()) / 1000D;
        result[10] = getMeanBytes();
        result[11] = getMaxBytes();
        result[12] = getMeanRate();
        result[13] = getMaxRate();
        return result;
    }

    /**
     * @param totalTotalTime
     * @param threadload 
     * @param elapsedtime 
     * @return
     */
    public double[] getFreqDataArray(double totalTotalTime, double threadload, float elapsedtime) {
        double[] result = new double[14];
        result[0] = nofCalls;
        result[1] = frequency;
        result[2] = minFrequency;
        result[3] = getMeanFrequency();
        result[4] = maxFrequency;
        result[5] = getFrequencyStandardDeviation();
        result[6] = (getMeanTime() > 0 ? 1000D * Math.max(1D, threadload) / getMeanTime() : 0D);
        result[7] = 100D * (totalTime / Math.max(totalTotalTime, Double.MIN_VALUE));
        result[8] = (elapsedtime > 0 ? nofCalls / Math.max(elapsedtime, Double.MIN_VALUE) : 0D);
        result[9] = (getMeanTime() + 3D * getTimeStandardDeviation()) / 1000D;
        result[10] = getMeanBytes();
        result[11] = getMaxBytes();
        result[12] = getMeanRate();
        result[13] = getMaxRate();
        return result;
    }
}
