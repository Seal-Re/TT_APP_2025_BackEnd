package com.seal.ttapp_base.util;


import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class HttpHelper {
    public static String getIpAddress(HttpServletRequest request) {
        String ip;
        Enumeration<String> xForwardeds = request.getHeaders("X-FORWARDED-FOR");
        if (xForwardeds == null || !xForwardeds.hasMoreElements()) {
            ip = request.getHeader("Proxy-Client-IP");
        } else {
            ip = xForwardeds.nextElement();
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
