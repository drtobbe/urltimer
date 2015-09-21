package com.mannetroll.urltimer.tail.impl;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.parser.SquidMessageParser;
import com.mannetroll.urltimer.tail.LogParser;
import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
public class SquidLogParser implements LogParser {
    private final static Logger logger = LoggerFactory.getLogger(SquidLogParser.class);
    private final static AbstractTimerInfoStats statistics = TimerInfoStats.getInstance("UrlTimer");
    private static final char SEP = '|';
    private static long index = 0L;
    private static long start = System.currentTimeMillis();

    public void parse(String line) {
        index++;
        if ((index % (100 * 1000)) == 0) {
            long elapsed = (System.currentTimeMillis() - start);
            logger.info("index: " + index + ", lines/sec: " + 1000F * index / (elapsed + 1F) + ", elapsed: " + elapsed);
        }
        try {
            Matcher squidMatcher = SquidMessageParser.match(line);
            if (!squidMatcher.matches()) {
                logger.error("err: " + line);
            } else {
                String date = squidMatcher.group(1).trim();
                String responseTime = squidMatcher.group(2).trim();
                String cache_result = squidMatcher.group(4).trim();
                String httpStatusCode = squidMatcher.group(5).trim();
                String numOfBytes = squidMatcher.group(6).trim();
                String verb = squidMatcher.group(7).trim();
                String url = squidMatcher.group(8).trim();
                //
                //logger.info("url: " + url);

                int bytes = getInteger(numOfBytes);
                long now = getNow(date);
                double responsetime_ms = getInteger(responseTime);

                if (!statistics.isAddQueryToKey()) {
                    int indexOf = url.indexOf('?');
                    if (indexOf > 0) {
                        url = url.substring(0, indexOf);
                    }
                } else {
                    url = url.replace('?', SEP);
                    url = url.replace('&', SEP);
                }
                String key = verb + SEP + httpStatusCode + SEP + cache_result + SEP + url;
                if (logger.isDebugEnabled()) {
                    logger.debug("key: " + key);
                }
                statistics.addCall(key, responsetime_ms, bytes, now);
                statistics.addTotalTime(responsetime_ms, bytes, now);
                statistics.addStatusCode(httpStatusCode);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Integer getInteger(String object) {
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception e) {
            throw new RuntimeException("Integer: " + object);
        }
    }

    private long getNow(String timestamp) {
        try {
            Double millis = 1000D * Double.parseDouble(timestamp);
            return millis.longValue();
        } catch (Exception e) {
            throw new RuntimeException("Timestamp: " + timestamp);
        }
    }

}
