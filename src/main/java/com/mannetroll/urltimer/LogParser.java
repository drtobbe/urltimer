package com.mannetroll.urltimer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

public class LogParser {
    private final static Logger logger = LoggerFactory.getLogger(LogParser.class);
    private final static AbstractTimerInfoStats statistics = TimerInfoStats.getInstance();
    private static final String UTF_8 = "UTF-8";
    private static final String SEP = "|";
    SimpleDateFormat accesslogDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    /*Log file feilds*/
    String clientHost = null;
    String date = null;
    String clientRequest = null;
    String httpStatusCode = null;
    String numOfBytes = null;
    String responseTime = null;

    public LogParser() {
    }

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

    public void parse(File file) {
        logger.info("file: " + file);
        try {
            BufferedReader bufferReader = new BufferedReader(new FileReader(file));
            String line = "";
            long index = 0;

            Pattern accessLogPattern = Pattern.compile(getAccessLogRegex(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher accessLogEntryMatcher;
            while ((line = bufferReader.readLine()) != null) {
                if ((index % 1000) == 0) {
                    logger.info("index: " + index);
                }
                index++;
                accessLogEntryMatcher = accessLogPattern.matcher(line);
                if (!accessLogEntryMatcher.matches()) {
                    System.out.println("err: " + index + " : " + line);
                    break;
                    //continue;
                } else {
                    date = accessLogEntryMatcher.group(4);
                    clientRequest = accessLogEntryMatcher.group(5);
                    httpStatusCode = accessLogEntryMatcher.group(6);
                    numOfBytes = accessLogEntryMatcher.group(7);
                    responseTime = accessLogEntryMatcher.group(8);
                    //
                    String[] split = clientRequest.split(" ");
                    if (split.length > 1) {
                        String verb = split[0];
                        String url = split[1];
                        String[] tmp = url.split("\\?");
                        if (tmp.length > 1) {
                            String query = tmp[1];
                            Map<String, String> splitQuery = splitQuery(query);
                            String uri = tmp[0];
                            String[] segment = uri.split("/");
                            if (segment.length > 1) {
                                String key = segment[segment.length - 1];
                                key = verb + SEP + httpStatusCode + SEP + key;
                                for (String string : splitQuery.keySet()) {
                                    key += SEP + string + "=" + splitQuery.get(string);
                                }
                                //logger.info("key: " + key);
                                long now = getNow(date);
                                int chunk = getInteger(numOfBytes);
                                double responsetime_ms = getInteger(responseTime) / 1000;
                                if ("200".equals(httpStatusCode) && "GET".equals(verb)) {
                                    statistics.addCall(key, responsetime_ms, chunk, now);
                                    statistics.addTotalTime(responsetime_ms, chunk, now);
                                }
                            }
                        }
                    }
                }
            }
            bufferReader.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {

        }
    }

    private Integer getInteger(String object) {
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private long getNow(String timestamp) {
        try {
            Date parse = accesslogDateFormat.parse(timestamp);
            return parse.getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

    public Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new TreeMap<String, String>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String param = URLDecoder.decode(pair.substring(0, idx), UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), UTF_8);
                query_pairs.put(param, value);
            }
        }
        return query_pairs;
    }
}
