package com.mannetroll.urltimer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author drtobbe
 */
public class MessageParser {
    private final static Logger logger = LoggerFactory.getLogger(MessageParser.class);
    private static Pattern accessLogPattern = Pattern.compile(getAccessLogRegex(), Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);

    /**
    * Structures Apache common access log
    *@return regex
    */
    static String getAccessLogRegex() {
        String clientHost = "^([\\d.]+)"; // Client IP
        String regex2 = " (\\S+)"; // -
        String user = " (\\S+)"; // -
        String date = " \\[([\\w:/]+\\s[+\\-]\\d{4})\\]"; // Date
        String request = " \"(.+?)\""; // request method and url
        String httpStatusCode = " (\\d{3}|-)"; // HTTP code
        String numOfBytes = " (\\d+|-)"; // Number of bytes
        String responseTime = " (\\d+|-)"; // Response times
        return clientHost + regex2 + user + date + request + httpStatusCode + numOfBytes + responseTime;
    }

    public static Matcher match(String line) {
        if (logger.isDebugEnabled()) {
            logger.debug("line: " + line);
        }
        return accessLogPattern.matcher(line);
    }

}
