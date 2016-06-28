package com.mannetroll.urltimer;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
public class LogParserTest {
    private final static Logger logger = LoggerFactory.getLogger(LogParserTest.class);

    @Test
    public void test() {
        FileLogParser logParser = new FileLogParser();
        File file = new File("src/test/resources/test.log");
        logParser.parse(file);
        logger.info("avg: " + TimerInfoStats.getInstance().getAverage());
        TimerInfoStats.getInstance().clear();
    }

}
