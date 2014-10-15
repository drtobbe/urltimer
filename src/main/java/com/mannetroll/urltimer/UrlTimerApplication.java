package com.mannetroll.urltimer;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class UrlTimerApplication extends SpringBootServletInitializer {
    private final static Logger logger = LoggerFactory.getLogger(UrlTimerApplication.class);
    private final static AbstractTimerInfoStats statistics = TimerInfoStats.getInstance();
    private final static Random randomGenerator = new Random();

    public static void main(String[] args) {
        logger.info("Start");
        boolean debug = false;
        if (debug) {
            fakeCall("GET|200|FAKE1");
            sleep(1000);
            fakeCall("GET|200|FAKE2");
            int length = 2000000;
            for (int i = 0; i < length; i++) {
                fakeCall("GET|200|FAKE3");
            }
        }
        SpringApplication.run(UrlTimerApplication.class, args);
        logger.info("Done");
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private static void fakeCall(String key) {
        long responsetime_ms = Math.round(getGaussian(2000, 5));
        int chunk = (int) Math.round(getGaussian(15 * 1024, 5));
        long now = System.currentTimeMillis();
        statistics.addCall(key, responsetime_ms, chunk, now);
        statistics.addTotalTime(responsetime_ms, chunk, now);
    }

    private static double getGaussian(double a, double b) {
        return Math.round(Math.max(0D, a * (1.0D + randomGenerator.nextGaussian() / b)));
    }

}