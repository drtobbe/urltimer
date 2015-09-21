package com.mannetroll.urltimer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author drtobbe
 */
public class SquidMessageParser {
    private final static Logger logger = LoggerFactory.getLogger(SquidMessageParser.class);
    private static Pattern accessLogPattern = Pattern.compile(getSquidLogRegex(), Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);

    /**
    * Structures Squid common access log
    *@return regex
    */
    public static String getSquidLogRegex() {
        String timeStamp = "(\\d+\\.\\d+\\s+)"; // UNIX
        String restime = "(\\d+)"; // Response times        
        String host = " ([\\d.]+)"; // Client IP
        String cache_result = " (\\b\\w+\\b)"; // cache_result
        String response = "/(\\d+)"; // response
        String bytes = " (\\d+)"; // Number of bytes
        String verb = " (\\b\\w+\\b\\s+)"; // request method
        String request = "(.+)"; // request url
        String user = " (\\S+)"; // user
        String type = " (\\S+\\/\\S+|-)"; // type
        return timeStamp + restime + host + cache_result + response + bytes + verb + request + user + type + type;
    }

    public static Matcher match(String line) {
        if (logger.isDebugEnabled()) {
            logger.debug("line: " + line);
        }
        return accessLogPattern.matcher(line);
    }
    
    public static void main(String[] args) {
        String line = "1427666402.954   1747 10.135.138.44 TCP_MISS/200 18959 GET http://gw.cint.postdk.net/Rest/Information/Pub/BusinessLocation/ServicePointService_v0100/BusinessLocationLocator/Logistics/ServicePointService_1.0/findNearestByAddress.json?consumerId=a44af1f9-a61d-4343-a858-2dbcafdf41b5&countryCode=SE&postalCode=12264&numberOfServicePoints=15 - FIRSTUP_PARENT/127.0.0.1 application/json";
        Matcher matcher = match(line);
        if (!matcher.matches()) {
            System.out.println("err: " + line);
        }
        int groupCount = matcher.groupCount();
        for (int i = 1; i < groupCount+1; i++) {
            System.out.println(i + ": <" + matcher.group(i).trim() + ">");
        }        
    }

}
