package com.mannetroll.urltimer.tail;

import java.io.File;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.tail.impl.SquidLogParser;

/**
 * @author drtobbe
 */
public class SquidLogTailer {
    public static final String SQUID_LOGTAILER_FILENAME = "SQUID_LOGTAILER_FILENAME";
    private final static Logger logger = LoggerFactory.getLogger(SquidLogTailer.class);

    public static void main(String[] args) {
        SquidLogTailer tmp = new SquidLogTailer();
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
        TailerListener listener = new LogParserTailerListener(new SquidLogParser());
        String filename = System.getenv(SQUID_LOGTAILER_FILENAME) != null ? System.getenv(SQUID_LOGTAILER_FILENAME)
                : "src/test/resources/squid.log";
        File file = new File(filename);
        if (file.exists()) {
            logger.info("###### SQUID_LOGTAILER_FILENAME: " + filename);
            long delay = 1000;
            Tailer tailer = Tailer.create(file, listener, delay);
            return tailer;
        } else {
            logger.error("###### FileNotFound: " + filename);
        }
        return null;
    }
}
