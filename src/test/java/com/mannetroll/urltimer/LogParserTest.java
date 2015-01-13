package com.mannetroll.urltimer;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

public class LogParserTest {
    private final static Logger logger = LoggerFactory.getLogger(LogParserTest.class);

    @Test
    public void test() {
        LogParser logParser = new LogParser();
        File file = new File("src/test/resources/test.log");
        logParser.parse(file);
        logger.info("avg: " + TimerInfoStats.getInstance("UrlTimer").getAverage());
        TimerInfoStats.getInstance("UrlTimer").clear();
    }

}
