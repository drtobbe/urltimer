package com.mannetroll.urltimer.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 */
@Controller
public class SinkController {
    private final static Logger logger = LoggerFactory.getLogger(SinkController.class);
    private final AbstractTimerInfoStats statistics = TimerInfoStats.getInstance("UrlTimer");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
    private final static Integer ZERO = Integer.valueOf(0);
    private static final char SEP = '|';

    @RequestMapping(value = "/jsonsink", method = RequestMethod.POST, headers = "Accept=application/json;charset=utf-8")
    @ResponseBody
    public String register(@RequestBody Map<String, Object> map) {
        try {
            //
            String request = (String) map.get("request");
            if (request == null) {
                request = "-";
            }
            String response = (String) map.get("response");
            String verb = (String) map.get("verb");
            Integer responsetime = ((Integer) map.get("responsetime_ms"));
            double timeSlice = 0D;
            if (responsetime != null) {
                timeSlice = Double.valueOf(responsetime) / 1000D;
            }
            Integer bytes = getBytes(map.get("bytes"));
            //
            String timestamp = (String) map.get("timestamp");
            long now = getNow(timestamp);
            //
            if (!statistics.isAddQueryToKey()) {
                int indexOf = request.indexOf('?');
                if (indexOf > 0) {
                    request = request.substring(0, indexOf);
                }
            }
            request = request.replace('?', SEP);
            request = request.replace('&', SEP);
            String key = verb + SEP + response + SEP + request;
            if (logger.isDebugEnabled()) {
                logger.debug("key: " + key);
            }
            statistics.addCall(key, timeSlice, bytes, now);
            statistics.addTotalTime(timeSlice, bytes, now);
            statistics.addStatusCode(response);
        } catch (Exception e) {
            logger.error("body: " + map, e);
        }
        return "";
    }

    private Integer getBytes(Object object) {
        Integer bytes = SinkController.ZERO;
        if (object instanceof Integer) {
            bytes = (Integer) object;
        } else if (object != null) {
            bytes = Integer.parseInt(object.toString());
        }
        return bytes;
    }

    private long getNow(String timestamp) {
        try {
            Date parse = timeFormat.parse(timestamp);
            return parse.getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }


}