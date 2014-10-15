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
import com.mongodb.BasicDBObject;

/**
 * @author drtobbe
 */
@Controller
@RequestMapping("/sink")
public class SinkController {
    private final static Logger logger = LoggerFactory.getLogger(SinkController.class);
    private final AbstractTimerInfoStats statistics = TimerInfoStats.getInstance();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
    private final static Integer ZERO = Integer.valueOf(0);
    private final static Integer KILO = Integer.valueOf(1000);

    @RequestMapping(value = "/json", method = RequestMethod.POST, headers = "Accept=application/json;charset=utf-8")
    @ResponseBody
    public String register(@RequestBody String body) {
        try {
            BasicDBObject call = (BasicDBObject) com.mongodb.util.JSON.parse(body);
            @SuppressWarnings("rawtypes")
            Map map = call.toMap();
            //
            String request = (String) map.get("request");
            String response = (String) map.get("response");
            String verb = (String) map.get("verb");
            Integer responsetime = (Integer) map.get("responsetime_ms");
            if (responsetime == null) {
                responsetime = SinkController.KILO;
            }
            Integer responsetime_ms = responsetime / 1000;
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
            request = request.replace('?', '|');
            request = request.replace('&', '|');
            String key = verb + "|" + response + "|" + request;
            if (logger.isDebugEnabled()) {
                logger.debug("key: " + key);
            }
            statistics.addCall(key, responsetime_ms, bytes, now);
            statistics.addTotalTime(responsetime_ms, bytes, now);
            statistics.addStatusCode(response);
        } catch (Exception e) {
            logger.error("body: " + body, e);
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