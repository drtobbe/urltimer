package com.mannetroll.urltimer;

import java.io.File;

import org.junit.Test;

import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

public class LogParserTest {

    @Test
    public void test() {
        LogParser logParser = new LogParser();
        File file = new File("src/test/resources/test.log");
        logParser.parse(file);
        TimerInfoStats.getInstance().clear();
    }

}
