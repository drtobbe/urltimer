package com.mannetroll.urltimer.tail;

import org.apache.commons.lang.NotImplementedException;

/**
 * @author drtobbe
 */
public interface LogParser {

    void parse(String line);

    /**
     * NULL implementation
     */
    public static final LogParser NULL = new LogParser() {

        @Override
        public void parse(String line) {
            throw new NotImplementedException(LogParser.class);
        }

    };
}