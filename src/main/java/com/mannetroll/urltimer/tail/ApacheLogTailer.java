package com.mannetroll.urltimer.tail;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.tail.impl.ApacheLogParser;

/**
 * @author drtobbe
 */
public class ApacheLogTailer {
    public static final String APACHE_LOGTAILER_FILENAME = "APACHE_LOGTAILER_FILENAME";
    private final static Logger logger = LoggerFactory.getLogger(ApacheLogTailer.class);

    public static void main(String[] args) {
        ApacheLogTailer tmp = new ApacheLogTailer();
        Tailer tailer = tmp.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        tailer.stop();
        logger.info("Done!");
    }

    public Tailer start() {
        TailerListener listener = new LogParserTailerListener(new ApacheLogParser());
        String filename = System.getenv(APACHE_LOGTAILER_FILENAME) != null ? System.getenv(APACHE_LOGTAILER_FILENAME)
                : "src/test/resources/apache.log";
        File file = new File(filename);
        if (file.exists()) {
            logger.info("###### APACHE_LOGTAILER_FILENAME: " + filename);
            long delay = 1000;
            Tailer tailer = Tailer.create(file, listener, delay);
            return tailer;
        } else {
            logger.error("###### FileNotFound: " + filename);
        }
        return null;
    }
}
