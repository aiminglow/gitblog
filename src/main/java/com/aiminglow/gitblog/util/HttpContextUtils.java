package com.aiminglow.gitblog.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @ClassName HttpContextUtils
 * @Description 获取request和session的工具类
 * @Author aiminglow
 */
public class HttpContextUtils {
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpSession getSession() {
        return getHttpServletRequest().getSession();
    }
}
