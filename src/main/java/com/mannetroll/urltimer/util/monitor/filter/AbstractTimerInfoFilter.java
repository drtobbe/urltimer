package com.mannetroll.urltimer.util.monitor.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mannetroll.urltimer.util.monitor.statistics.AbstractTimerInfoStats;

/**
 * @author drtobbe
 */
public abstract class AbstractTimerInfoFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(AbstractTimerInfoFilter.class);
    private static final String TAB = "                   ";
    private static final String NL = "\r\n" + TAB;
    private static int calls = 0;
    private static final String BR = " <br> ";
    private final AbstractTimerInfoStats statistics;
    private final String name;
    private static final String ETAGCACHE = "EtagCache";

    /**
     * Default constructor
     * @param statistics
     */
    public AbstractTimerInfoFilter(AbstractTimerInfoStats statistics, String name) {
        this.statistics = statistics;
        this.name = name;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        //
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        StringBuffer sb = new StringBuffer();
        if (logger.isInfoEnabled()) {
            sb.append("################################################################### " + name).append(NL);
            sb.append((calls++) + ": Request: " + getUrl(request)).append(NL);
        }
        long start = System.currentTimeMillis();
        StatusHttpServletResponseWrapper responseWrapper = new StatusHttpServletResponseWrapper(response);
        try {
            addIpToResponse(responseWrapper);
            // Do Filter Chain
            filterChain.doFilter(servletRequest, responseWrapper);
        } catch (ServletException e) {
            String msg = "X-ServletException: ";
            Throwable rootCause = e.getRootCause();
            if (rootCause != null) {
                msg += rootCause.getClass().getName() + ": " + BR + rootCause.getMessage() + NL;
                logger.warn("ServletException: " + rootCause.getMessage(), rootCause);
            } else {
                msg += "RootCause = null";
            }
            msg += getUrlInfo(request);
            statistics.addCall(msg, (System.currentTimeMillis() - start), System.currentTimeMillis());
            throw e;
        } catch (IOException e) {
            String msg = "X-IOException: " + BR + e.getMessage() + BR + getUrlInfo(request);
            statistics.addCall(msg, (System.currentTimeMillis() - start), System.currentTimeMillis());
            throw e;
        } catch (RuntimeException e) {
            String msg = "X-" + e.getClass().getName() + ": " + BR + e.getMessage() + BR + getUrlInfo(request);
            statistics.addCall(msg, (System.currentTimeMillis() - start), System.currentTimeMillis());
            throw e;
        }
        long filterTime = System.currentTimeMillis();
        String key = responseWrapper.getStatus() + request.getRequestURI();
        if (statistics.isAddQueryToKey()) {
            String query = request.getQueryString();
            if (query != null) {
                key += "?" + query;
            }
        }
        long now = System.currentTimeMillis();
        statistics.addCall(key, filterTime, now);
        statistics.addTotalTime(filterTime, 0, now);
        if (logger.isInfoEnabled()) {
            sb.append("key: " + key).append(NL);
            sb.append("elapsed: " + filterTime).append(NL);
            logger.info(sb.toString());
        }
        if (key.startsWith("304")) {
            statistics.addNotModified();
        }
        if (key.startsWith("200C")) {
            statistics.addEtagCached();
        }
        if (key.startsWith("200R")) {
            statistics.addRendered();
        }
    }

    private void addIpToResponse(HttpServletResponse response) {
        response.setHeader("X-SEMC-HOST-NAME", getHostName());
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Throwable th) {
            // Akamai EdgeJava will throw exception, with IP as message
            return th.getMessage();
        }
    }

    private String getUrl(HttpServletRequest request) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(request.getRequestURI());
        String queryString = request.getQueryString();
        if (queryString != null) {
            stringBuffer.append("?");
            stringBuffer.append(queryString);
        }
        return stringBuffer.toString();
    }

    private String getUrlInfo(HttpServletRequest request) {
        StringBuffer stringBuffer = new StringBuffer(request.getMethod()).append(" ");
        stringBuffer.append(getUrl(request)).append(NL);
        stringBuffer.append("host: ").append(request.getHeader("host")).append(NL);
        stringBuffer.append("cookie: ").append(request.getHeader("cookie")).append(NL);
        stringBuffer.append("referer: ").append(request.getHeader("referer")).append(NL);
        stringBuffer.append("fromes: ").append(request.getHeader("fromes")).append(NL);
        stringBuffer.append("True-Client-IP: ").append(request.getHeader("True-Client-IP")).append(NL);
        return stringBuffer.toString();
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    private static class StatusHttpServletResponseWrapper extends HttpServletResponseWrapper {
        private int status;
        private String etagcache = "";
        public StatusHttpServletResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        @Override
        public void setHeader(String name, String value) {
            if (ETAGCACHE.equalsIgnoreCase(name)) {
                etagcache = value;
            }
            super.setHeader(name, value);
        }
        @Override
        public void setStatus(int sc) {
            this.status = sc;
            super.setStatus(sc);
        }
        public int getStatus() {
            return status;
        }
    }

}