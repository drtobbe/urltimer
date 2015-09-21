package com.mannetroll.urltimer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author drtobbe
 */
public class ApacheMessageParser {
    private final static Logger logger = LoggerFactory.getLogger(ApacheMessageParser.class);
    private static Pattern accessLogPattern = Pattern.compile(getAccessLogRegex(), Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);

    /**
    * Structures Apache common access log
    *@return regex
    */
    public static String getAccessLogRegex() {
        String clientHost = "^([\\d.]+)"; // Client IP
        String regex2 = " (\\S+)"; // -
        String user = " (\\S+)"; // -
        String date = " \\[([\\w:/]+\\s[+\\-]\\d{4})\\]"; // Date
        String verb = " \"(\\b\\w+\\b)"; // request method
        String request = " (.+?)\""; // request url
        String httpStatusCode = " (\\d{3}|-)"; // HTTP code
        String numOfBytes = " (\\d+|-)"; // Number of bytes
        String responseTime = " (\\d+|-)"; // Response times
        return clientHost + regex2 + user + date + verb + request + httpStatusCode + numOfBytes + responseTime;
    }

    public static Matcher match(String line) {
        if (logger.isDebugEnabled()) {
            logger.debug("line: " + line);
        }
        return accessLogPattern.matcher(line);
    }

    public static void main(String[] args) {
        String line = "10.135.138.44 - - [01/Oct/2014:02:00:02 +0200] \"GET /Rest/Information/Pub/BusinessLocation/ServicePointService_v0100/BusinessLocationLocator/Logistics/ServicePointService_1.0/findNearestByAddress.json?consumerId=3de41b18-1234-4567-91dd-08018218ab2f&countryCode=DK&postalCode=1000&numberOfServicePoints=15 HTTP/1.1\" 200 17708 1126309";
        Matcher matcher = match(line);
        if (!matcher.matches()) {
            System.out.println("err: " + line);
        }
        int groupCount = matcher.groupCount();
        for (int i = 1; i < groupCount + 1; i++) {
            System.out.println(i + ": <" + matcher.group(i).trim() + ">");
        }
    }

}
