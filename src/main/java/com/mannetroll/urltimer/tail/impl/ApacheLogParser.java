package com.mannetroll.urltimer.tail.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.parser.ApacheMessageParser;
import com.mannetroll.urltimer.tail.LogParser;
import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
public class ApacheLogParser implements LogParser {
    private final static Logger logger = LoggerFactory.getLogger(ApacheLogParser.class);
    private final static AbstractTimerInfoStats statistics = TimerInfoStats.getInstance("UrlTimer");
    private static final char SEP = '|';
    private static long index = 0L;
    private static long start = System.currentTimeMillis();
    private static SimpleDateFormat accesslogDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    public void parse(String line) {
        index++;
        if ((index % (100 * 1000)) == 0) {
            long elapsed = (System.currentTimeMillis() - start);
            logger.info("index: " + index + ", lines/sec: " + 1000F * index / (elapsed + 1F) + ", elapsed: " + elapsed);
        }
        try {
            Matcher accessLogEntryMatcher = ApacheMessageParser.match(line);
            if (!accessLogEntryMatcher.matches()) {
                logger.error("err: " + line);
            } else {
                String date = accessLogEntryMatcher.group(4).trim();
                String verb = accessLogEntryMatcher.group(5).trim();
                String url = accessLogEntryMatcher.group(6).trim();
                String httpStatusCode = accessLogEntryMatcher.group(7).trim();
                String numOfBytes = accessLogEntryMatcher.group(8).trim();
                String responseTime = accessLogEntryMatcher.group(9).trim();
                //
                //logger.info("url: " + url);

                int bytes = getInteger(numOfBytes);
                long now = getNow(date);
                double responsetime_ms = getInteger(responseTime) / 1000D;

                if (!statistics.isAddQueryToKey()) {
                    int indexOf = url.indexOf('?');
                    if (indexOf > 0) {
                        url = url.substring(0, indexOf);
                    }
                } else {
                    url = url.replace('?', SEP);
                    url = url.replace('&', SEP);
                }
                String key = verb + SEP + httpStatusCode + SEP + url;
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
            Date parse = accesslogDateFormat.parse(timestamp);
            return parse.getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

}
