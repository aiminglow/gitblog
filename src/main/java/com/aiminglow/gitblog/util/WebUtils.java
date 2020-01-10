package com.aiminglow.gitblog.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName WebUtils
 * @Description 获取用户IP，user agent等信息的工具类
 * @Author aiminglow
 */
public class WebUtils {
    public static String getClientIp() {
        String remoteAddr = null;
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARD-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    public static String getUserAgent() {
        String userAgent = null;
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if (request != null) {
            userAgent = request.getHeader("User-Agent");
        }
        return userAgent;
    }

    public static String getUri() {
        String uri = null;
        HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
        if (request != null) {
            uri = request.getRequestURI();
        }
        return uri;
    }
}
