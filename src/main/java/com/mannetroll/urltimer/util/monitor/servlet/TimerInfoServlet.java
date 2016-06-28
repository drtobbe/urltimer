package com.mannetroll.urltimer.util.monitor.servlet;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoItem;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoMemory;
import com.mannetroll.urltimer.util.monitor.statistics.TimerInfoStats;

/**
 * @author drtobbe
 *
 * http://localhost:8181/timerinfo/info?stat=Performance
 */
@Configuration
public class TimerInfoServlet extends HttpServlet {
    private static final String E = "";
    private static final long serialVersionUID = 1L;
    private static TimerInfoStats tistat = TimerInfoStats.getInstance();
    private static final String NAME = tistat.getName();
    private static long serverStart = tistat.getServerStart();

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        return new ServletRegistrationBean(new TimerInfoServlet(), "/info");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Edge-control", "no-store");
        if ("clear".equalsIgnoreCase(request.getParameter("clear"))) {
            tistat.clear();
        }
        ServletOutputStream out = response.getOutputStream();
        String datestr = getHostName() + " - " + (new SimpleDateFormat("yyyyMMdd - HHmmss")).format(new Date());
        //
        // xml
        if (request.getParameter("memxml") != null) {
            memoryXml(out, tistat.getMemoryInfo().getValues(), 1, "JVM Memory - " + datestr);
            out.flush();
            return;
        } else if (request.getParameter("histxml") != null) {
            memoryXml(out, tistat.getResponseTimes(), 1, "Response Histogram - " + datestr);
            out.flush();
            return;
        }
        //
        // html
        if (request.getParameter("qon") != null) {
            tistat.setAddQueryToKey(true);
        } else if (request.getParameter("qoff") != null) {
            tistat.setAddQueryToKey(false);
        }
        String sort = request.getParameter("sort");
        String baseurl = request.getRequestURI();
        if (request.getParameter("freq") != null) {
            header(NAME + " - Frequency - " + datestr, false, out);
            baseurl += "?freq=Frequency";
            frequencyTable(out, tistat, "X", null, sort, baseurl);
            footer(out);
        } else if (request.getParameter("mem") != null) {
            header(NAME + " - JVM Memory - " + datestr, true, out);
            String title = "JVM Memory";
            pixelPlot(out, tistat.getMemoryInfo().getValues(), 1, "navy", 60, title);
            footer(out);
        } else if (request.getParameter("payload") != null) {
            header(NAME + " - Payload Histogram - " + datestr, false, out);
            String title = "Payload";
            pixelPlot(out, tistat.getChunkSizes(), 0, "green", 60, title);
            footer(out);
        } else {
            boolean reload = false;
            if (request.getParameter("reload") != null) {
                reload = true;
            }
            NumberFormat nf0 = NumberFormat.getInstance();
            nf0.setMaximumFractionDigits(0);
            nf0.setMinimumFractionDigits(0);
            header(NAME + " - Performance - " + datestr, reload, out);
            baseurl += "?stat=Performance";
            statisticsTable(out, tistat, "X", null, sort, baseurl);
            String title1 = " Response " + nf0.format(tistat.getAverage()) + " [ms]";
            pixelPlot(out, tistat.getResponseTimes(), 0, "red", 55, title1);
            String title2 = "Bitrate " + nf0.format(tistat.getBitrateAverage() / 1024D) + " [KB/s]";
            pixelPlot(out, tistat.getRatesSizes(), 1, "blue", 270, title2);
            footer(out);
        }
        out.flush();
    }

    private void memoryXml(ServletOutputStream out, long[] values, int gid, String title) throws IOException {
        int size = values.length;
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<chart>");
        out.println("<series>");
        for (int i = 0; i < size; i++) {
            out.println("<value xid=\"" + i + "\">" + i + "</value>");
        }
        out.println("</series>");
        out.println("<graphs>");
        out.println("<graph gid=\"" + gid + "\" title=\"" + title + "\">");
        for (int i = 0; i < size; i++) {
            out.println("<value xid=\"" + i + "\">" + values[i] / 3 + "</value>");
        }
        out.println("</graph>");
        out.println("</graphs>");
        out.println("</chart>");
    }

    /**
     * @param title
     * @param reload
     * @param out
     * @throws IOException
     */
    private void header(String title, boolean reload, ServletOutputStream out) throws IOException {
        out.println("<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.0 Transitional//EN'>");
        out.println("<html>");
        out.println("<head>");
        if (reload) {
            out.println("<meta http-equiv=\"refresh\" content=\"5\">");
        }
        out.println("<style type='text/css'>");
        out.println("  td {");
        out.println("      border-bottom: 1px solid black;");
        out.println("      border-right: 1px solid black;");
        out.println("      font-family: verdana;");
        out.println("      font-size: 10px;");
        out.println("     }");
        out.println("  th {");
        out.println("      border-bottom: 1px solid black;");
        out.println("      font-family: verdana;");
        out.println("      font-size: 12px;");
        out.println("     }");
        out.println("  .btext {");
        out.println("      font-family: verdana;");
        out.println("      font-size: 12px;");
        out.println("      background-color: #eeeeff;");
        out.println("      border: 1px solid;");
        out.println("      width: 85;");
        out.println("      padding: 1px;");
        out.println("      margin: 1px;");
        out.println("     }");
        out.println("</style>");
        out.println("<title>" + title + "</title>");
        out.println("</head>");
        out.println("<body style='margin-left: 20px'>");
        out.println("<h2>" + title + "</h2>");

        TimerInfoMemory memoryBucket = tistat.getMemoryInfo();
        long totalcall = tistat.getTotalNofCalls();
        float totaltime = ((tistat.getLastTime() - tistat.getCreateTime()) / 1000f); // secs
        out.println("<table cellspacing='0' cellpadding='1'><tr><td style='border:none'>");
        out.println("<table style='border:1px solid black' cellspacing='0' cellpadding='1'>");
        out.println("<tr bgcolor='#eeeeff'><th>&nbsp;</th><th>&nbsp;</th></tr>");
        float callssecs = totalcall / totaltime;
        double threadload = (tistat.getTotalAverage() * callssecs) / 1000f;
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss", new Locale("en", "US"));
        out.println(createRow("CALLs/minute", E + callssecs * 60));
        out.println(createRow("CALLs/second", E + callssecs));
        out.println(createRow("Number of URI", E + tistat.getStatDataSize()));
        out.println(createRow("Number of calls (S)", E + totalcall));
        out.println(createRow("Number of calls (M)", E + tistat.getSumM1()));
        String hits = tistat.get200OK() + " / " + tistat.get50X() + " / " + tistat.get40X() + " / " + tistat.get30X();
        out.println(createRow("200 / 50X / 40X / 30X", E + hits));
        out.println(createRow("Elapsed time (min)", E + totaltime / 60));
        out.println(createRow("Startup memory (Mb)", E + (memoryBucket.getMemStart() / 1024 / 1024)));
        out.println(createRow("Max memory (Mb)", E + (memoryBucket.getMemMax() / 1024 / 1024)));
        out.println(createRow("Free memory (Mb)", E + (memoryBucket.getFree() / 1024 / 1024)));
        out.println(createRow("Total memory (Mb)", E + (memoryBucket.getTotal() / 1024 / 1024)));
        out.println(createRow("Average (S)", E + tistat.getTotalAverage()));
        out.println(createRow("Average (M)", E + tistat.getAverageM1()));
        out.println(createRow("Payload [KB]", E + tistat.getPaylodAverage() / 1024));
        out.println(createRow("Bitrate [KB/s]", E + tistat.getBitrateAverage() / 1024));
        out.println(createRow("Median", E + tistat.getMedian()));
        out.println(createRow("Threadload (A)", E + threadload));
        out.println(createRow("Threadload (M)", E + (tistat.getMedian() * callssecs) / 1000f));
        out.println(createRow("AvailableProcessors", E + Runtime.getRuntime().availableProcessors()));
        out.println(createRow("OS", System.getProperty("os.name") + ", " + System.getProperty("os.version")));
        String jvm = System.getProperty("java.vm.name") + ", " + System.getProperty("java.vm.version");
        out.println(createRow("JVM", jvm));
        out.println(createRow("Server start", E + ((new Date(serverStart)))));
        out.println(createRow("Create date", E + ((new Date(tistat.getCreateTime())))));
        out.println(createRow("Last access", E + ((new Date(tistat.getLastTime())))));
        out.println("\r\n<tr bgcolor='#eeeeff'><td colspan='13' style='border:none'>&nbsp;</td></tr></table></td>");
        out.println("</tr></table>");
        out.println("<br/>");
        out.println("<table style='border:0px' cellspacing='0' cellpadding='1'>");
        out.println("<tr bgcolor='#fff'>");
        // buttons
        out.println("<td style='border:none'><form method=\"get\">");
        out.println("<input class=\"btext\" type=\"submit\" name=\"stat\" value=\"Performance\"/>");
        out.println("<input class=\"btext\" type=\"submit\" name=\"reload\" value=\"Autoreload\"/>");
        //out.println("<input class=\"btext\" type=\"submit\" name=\"mem\" value=\"Memory\"/>");
        //out.println("<input class=\"btext\" type=\"submit\" name=\"freq\" value=\"Frequency\"/>");
        //out.println("<input class=\"btext\" type=\"submit\" name=\"statxml\" value=\"Perf XML\"/>");
        if (tistat.isAddQueryToKey()) {
            out.println("<input class=\"btext\" type=\"submit\" name=\"qoff\" value=\"Query On\"/>");
        } else {
            out.println("<input class=\"btext\" type=\"submit\" name=\"qon\" value=\"Query Off\"/>");
        }
        out.println("<input class=\"btext\" type=\"submit\" name=\"clear\" value=\"Clear\"/>");
        out.println("</form></td>");

        out.println("</tr></table>");
    }

    private String createRow(String key, String value) {
        return "<tr><td>" + key + "</td><td style='border-right:none'>" + value + "</td></tr>";
    }

    /**
     * @param out
     * @throws IOException
     */
    private void footer(ServletOutputStream out) throws IOException {
        out.println("</body></html>");
    }

    /**
     * @return HostName
     */
    private String getHostName() {
        String host;
        try {
            host = java.net.InetAddress.getLocalHost().getHostName();
        } catch (Throwable th) {
            host = th.getMessage();
        }
        return host;
    }

    /**
     * @param out
     * @param values
     * @param divId
     * @param color
     * @param title  
     * @throws IOException
     */
    private void pixelPlot(ServletOutputStream out, long[] values, int divId, String color, int top, String title)
            throws IOException {
        out.println("<script type='text/javascript' language='javascript'>");
        out.println("function Scatter" + divId + "() {");
        out.println("this.range = [0,1];");
        out.println("this.top = 0;");
        out.println("this.id" + divId + " = \"myChart" + divId + "\";");
        out.println("this.left = 0;");
        out.println("this.height = 100;");
        out.println("this.width  = 100;");
        out.println("this.borderWidth = 2;");
        out.println("this.borderStyle = \"outset\";");
        out.println("this.lineWidth = 2;");
        out.println("this.parent = null;");
        out.println("this.hilightColor = \"" + color + "\";");
        out.println("this.getWrapperHTML" + divId + " = function () {");
        out.println("with(this)");

        out.println("return \"<div style='position:absolute;left:\" + left + \"px;\" +");
        out.println("  \"top:\" + top + \"px;\" +");
        out.println("  \"width:\" + width + \"px;\" +");
        out.println("  \"height:\" + height + \"px;\" +");
        out.println("  \"border-style:\" + borderStyle + \";\" +");
        out.println("  \"border-width:\" + borderWidth + \"px;'\" +");
        out.println("  \" id=\" + id" + divId + " + \"></div>\";");

        out.println("}");
        out.println("this.values" + divId + " = [[0,0]];");
        out.println("this.redraw" + divId + " = function() {");
        out.println("var tempstr" + divId + " = \"\";");
        out.println("with(this) {");
        out.println("values" + divId + " = []");
        for (int i = 0; i < values.length; i++) {
            out.println("values" + divId + "[values" + divId + ".length] = [" + i + ", " + values[i] + "];");
        }
        out.println("for(var i=0; i<values" + divId + ".length; i++) {");
        out.println("  tempstr" + divId + " += \"<div style='position:absolute;background-Color:\" + hilightColor +");
        out.println("  \";left:\" + (borderWidth + parseInt(values" + divId + "[i][0])) + \"px;\" +");
        out.println("  \"top:\" + (height - 2 * borderWidth - parseInt(values" + divId + "[i][1])) + \"px;\" +");
        out.println("  \"width:\" + lineWidth + \"px;height:\" + lineWidth + \"px'></div>\";");
        out.println("}");
        out.println("document.getElementById(this.id" + divId + ").innerHTML = tempstr" + divId + ";");
        out.println("}");
        out.println("}");
        out.println("this.create" + divId + "  = function() {");
        out.println("document.body.innerHTML += this.getWrapperHTML" + divId + "();");
        out.println("this.redraw" + divId + "();");
        out.println("}");
        out.println("}");
        out.println("var tg;");
        out.println("function plot" + divId + "(){");
        out.println("tg = new Scatter" + divId + "();");
        out.println("with(tg) {");
        out.println("top = " + top + ";");
        out.println("left = 400;");
        out.println("width = 600;");
        out.println("height = 190;");
        out.println("create" + divId + "();");
        out.println("}}");
        out.println("plot" + divId + "();");
        //title
        out.println("var x = document.createElement(\"h4\");");
        out.println("var t = document.createTextNode(\"" + title + "\");");
        out.println("x.appendChild(t);");
        out.println("document.getElementById(\"myChart" + divId + "\").appendChild(x);");
        out.println("</script>");
    }
    /**
     * @param out
     * @param stats
     * @param jump1
     * @param sort
     * @param baseurl
     * @throws IOException
     */
    private void statisticsTable(ServletOutputStream out, TimerInfoStats stats, String jump1, String jump2,
            String sort, String baseurl) throws IOException {
        Set<TimerInfoItem> treeSet;
        if ("perf".equalsIgnoreCase(sort)) {
            treeSet = new TreeSet<TimerInfoItem>(new AverageComparator());
        } else if ("uri".equalsIgnoreCase(sort)) {
            treeSet = new TreeSet<TimerInfoItem>(new UriComparator());
        } else {
            treeSet = new TreeSet<TimerInfoItem>(new TotalTimeComparator());
        }
        treeSet.addAll(stats.getStatData().values());
        float elapsedtime = tistat.getElapsedTime() / 1000f; // secs
        //threadload
        //long totalcall = tistat.getTotalNofCalls();
        //float callssecs = totalcall / elapsedtime;
        //double threadload = (tistat.getTotalAverage() * callssecs) / 1000f;
        //
        out.println("<table style='border:1px solid black' cellspacing='0' cellpadding='1'>");
        out.println("<tr bgcolor='#eeeeff'>");
        out.println("<th>No</th>");
        out.println("<th># Calls</th>");
        out.println("<th>Total [s]</th>");
        out.println("<th>Min [ms]</th>");
        out.println("<th>Avg [ms]</th>");
        out.println("<th>Max [ms]</th>");
        out.println("<th>Std [ms]</th>");
        out.println("<th><a href='" + baseurl + "&sort=perf'>Perf [#/s]</a></th>");
        out.println("<th><a href='" + baseurl + "&sort=time'>Total [%]</a></th>");
        out.println("<th>Rate [#/s]</th>");
        out.println("<th>Td99 [s]</th>");
        out.println("<th>Pay [KB]</th>");
        out.println("<th>Max [KB]</th>");
        out.println("<th>Bitrate [KB/s]</th>");
        out.println("<th>Max [KB/s]</th>");
        out.println("<th><a href='" + baseurl + "&sort=uri'>URI</a></th></tr>");
        NumberFormat nf0 = NumberFormat.getInstance();
        nf0.setMaximumFractionDigits(0);
        nf0.setMinimumFractionDigits(0);
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumFractionDigits(1);
        nf1.setMinimumFractionDigits(1);
        NumberFormat nf2 = NumberFormat.getInstance();
        nf2.setMaximumFractionDigits(2);
        nf2.setMinimumFractionDigits(2);
        NumberFormat nf3 = NumberFormat.getInstance();
        nf3.setMaximumFractionDigits(3);
        nf3.setMinimumFractionDigits(3);
        long loop = 1;
        //statisticsTable
        for (TimerInfoItem item : treeSet) {
            String key = item.getKey();
            if (jump1 != null && key.startsWith(jump1)) {
                continue;
            }
            if (jump2 != null && key.startsWith(jump2)) {
                continue;
            }
            double[] statDataArray = item.getTimeDataArray(stats.getTotalTotalTime(), 1d, elapsedtime);
            if (statDataArray[0] != 0) {
                out.println("<tr align='right'>");
                out.println("<td>" + nf0.format(loop++) + "</td>");
                out.println("<td>" + nf0.format(statDataArray[0]) + "</td>");
                out.println("<td>" + nf0.format(statDataArray[1]) + "</td>");
                out.println("<td bgcolor='#eeeeff'>" + nf1.format(statDataArray[2]) + "</td>");
                out.println("<td bgcolor='#dddddd'>" + nf1.format(statDataArray[3]) + "</td>");
                out.println("<td bgcolor='#eeeeff'>" + nf1.format(statDataArray[4]) + "</td>");
                out.println("<td>" + nf1.format(statDataArray[5]) + "</td>");
                out.println("<td>" + nf2.format(statDataArray[6]) + "</td>");
                out.println("<td>" + nf3.format(statDataArray[7]) + "</td>");
                out.println("<td>" + nf3.format(statDataArray[8]) + "</td>");
                if (key.contains("|200|") || key.equals("TOTAL")) {
                    out.println("<td>" + nf2.format(statDataArray[9]) + "</td>");
                    out.println("<td bgcolor='#eee'>" + nf1.format(statDataArray[10] / 1024) + "</td>");
                    out.println("<td>" + nf1.format(statDataArray[11] / 1024) + "</td>");
                    out.println("<td bgcolor='#eee'>" + nf1.format(statDataArray[12] / 1024) + "</td>");
                    out.println("<td>" + nf1.format(statDataArray[13] / 1024) + "</td>");
                } else {
                    out.println("<td>-</td>");
                    out.println("<td bgcolor='#eee'>-</td>");
                    out.println("<td>-</td>");
                    out.println("<td bgcolor='#eee'>-</td>");
                    out.println("<td>-</td>");
                }
                out.println("<td style='border-right:none' align='left'>" + key + "</td>");
            }
            out.println("</tr>");
            if (loop > 10000) {
                break;
            }
        }
        out.println("\r\n<tr bgcolor='#eeeeff'><td colspan='16' style='border:none'>&nbsp;</td></tr></table>");
    }

    private class TotalTimeComparator implements Comparator<TimerInfoItem> {
        @Override
        public int compare(TimerInfoItem o1, TimerInfoItem o2) {
            if (o1.getTotalTime() < o2.getTotalTime()) {
                return 1;
            }
            return -1;
        }
    }

    private class AverageComparator implements Comparator<TimerInfoItem> {
        @Override
        public int compare(TimerInfoItem o1, TimerInfoItem o2) {
            if (o1.getMeanTime() < o2.getMeanTime()) {
                return 1;
            }
            return -1;
        }
    }

    private class UriComparator implements Comparator<TimerInfoItem> {
        @Override
        public int compare(TimerInfoItem o1, TimerInfoItem o2) {
            return o1.getKey().compareTo(o2.getKey());
        }
    }

    private void frequencyTable(ServletOutputStream out, TimerInfoStats stats, String jump1, String jump2, String sort,
            String baseurl) throws IOException {
        Set<TimerInfoItem> treeSet;
        if ("perf".equalsIgnoreCase(sort)) {
            treeSet = new TreeSet<TimerInfoItem>(new AverageComparator());
        } else if ("uri".equalsIgnoreCase(sort)) {
            treeSet = new TreeSet<TimerInfoItem>(new UriComparator());
        } else {
            treeSet = new TreeSet<TimerInfoItem>(new TotalTimeComparator());
        }
        treeSet.addAll(stats.getStatData().values());
        float totaltime = tistat.getElapsedTime() / 1000f; // secs
        //threadload
        //long totalcall = tistat.getTotalNofCalls();
        //float callssecs = totalcall / totaltime;
        //double threadload = (tistat.getTotalAverage() * callssecs) / 1000f;
        //
        out.println("<br/>");
        out.println("<table style='border:1px solid black' cellspacing='0' cellpadding='1'>");
        out.println("<tr bgcolor='#eeeeff'>");
        out.println("<th>No</th>");
        out.println("<th># Calls</th>");
        out.println("<th>Total (s)</th>");
        out.println("<th>Min (ms)</th>");
        out.println("<th>Frequency (ms)</th>");
        out.println("<th>Max (ms)</th>");
        out.println("<th>Std (ms)</th>");
        out.println("<th><a href='" + baseurl + "&sort=perf'>Perf (#/s)</a></th>");
        out.println("<th><a href='" + baseurl + "&sort=time'>Total (%)</a></th>");
        out.println("<th>Rate (#/s)</th>");
        out.println("<th>Td99 [s]</th>");
        out.println("<th>Payload [KB]</th>");
        out.println("<th>Max [KB]</th>");
        out.println("<th>Bitrate [KB/s]</th>");
        out.println("<th>Max [KB/s]</th>");
        out.println("<th><a href='" + baseurl + "&sort=uri'>URI</a></th></tr>");
        NumberFormat nf0 = NumberFormat.getInstance();
        nf0.setMaximumFractionDigits(0);
        nf0.setMinimumFractionDigits(0);
        NumberFormat nf1 = NumberFormat.getInstance();
        nf1.setMaximumFractionDigits(1);
        nf1.setMinimumFractionDigits(1);
        NumberFormat nf2 = NumberFormat.getInstance();
        nf2.setMaximumFractionDigits(2);
        nf2.setMinimumFractionDigits(2);
        NumberFormat nf3 = NumberFormat.getInstance();
        nf3.setMaximumFractionDigits(3);
        nf3.setMinimumFractionDigits(3);
        long loop = 1;
        //frequencyTable
        for (TimerInfoItem item : treeSet) {
            String key = item.getKey();
            if (jump1 != null && key.startsWith(jump1)) {
                continue;
            }
            if (jump2 != null && key.startsWith(jump2)) {
                continue;
            }
            double[] freqDataArray = item.getFreqDataArray(stats.getTotalTotalTime(), 1d, totaltime);
            if (freqDataArray[0] != 0) {
                out.println("<tr align='right'>");
                out.println("<td>" + nf0.format(loop++) + "</td>");
                out.println("<td>" + nf0.format(freqDataArray[0]) + "</td>");
                out.println("<td>" + nf0.format(freqDataArray[1]) + "</td>");
                out.println("<td bgcolor='#eeeeff'>" + nf1.format(freqDataArray[2]) + "</td>");
                out.println("<td bgcolor='#dddddd'>" + nf1.format(freqDataArray[3]) + "</td>");
                out.println("<td bgcolor='#eeeeff'>" + nf1.format(freqDataArray[4]) + "</td>");
                out.println("<td>" + nf1.format(freqDataArray[5]) + "</td>");
                out.println("<td>" + nf2.format(freqDataArray[6]) + "</td>");
                out.println("<td>" + nf3.format(freqDataArray[7]) + "</td>");
                out.println("<td>" + nf3.format(freqDataArray[8]) + "</td>");
                if (key.contains("|200|") || key.equals("TOTAL")) {
                    out.println("<td>" + nf2.format(freqDataArray[9]) + "</td>");
                    out.println("<td bgcolor='#eee'>" + nf1.format(freqDataArray[10] / 1024) + "</td>");
                    out.println("<td>" + nf1.format(freqDataArray[11] / 1024) + "</td>");
                    out.println("<td bgcolor='#eee'>" + nf1.format(freqDataArray[12] / 1024) + "</td>");
                    out.println("<td>" + nf1.format(freqDataArray[13] / 1024) + "</td>");
                } else {
                    out.println("<td>-</td>");
                    out.println("<td bgcolor='#eee'>-</td>");
                    out.println("<td>-</td>");
                    out.println("<td bgcolor='#eee'>-</td>");
                    out.println("<td>-</td>");
                }
                out.println("<td style='border-right:none' align='left'>" + key + "</td>");
            }
            out.println("</tr>");
            if (loop > 10000) {
                break;
            }
        }
        out.println("\r\n<tr bgcolor='#eeeeff'><td colspan='16' style='border:none'>&nbsp;</td></tr></table>");
    }
}
