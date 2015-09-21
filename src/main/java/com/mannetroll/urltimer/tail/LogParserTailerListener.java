package com.mannetroll.urltimer.tail;

import org.apache.commons.io.input.TailerListenerAdapter;

/**
 * @author drtobbe
 */
public class LogParserTailerListener extends TailerListenerAdapter {
    private LogParser parser = LogParser.NULL;

    public LogParserTailerListener(LogParser parser) {
        this.parser = parser;
    }

    public void handle(String line) {
        parser.parse(line);
    }
}