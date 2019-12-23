package com.aiminglow.gitblog.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @ClassName UserLoginInterceptor
 * @Description 用户登录状态校验 拦截器：如果未登录，导向登录页面
 * TODO 之后还要做一个，如果已经登录，前往登录页面的话，直接导向用户主页或者其他页面
 * @Author aiminglow
 */
@Component
public class UserLoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        HttpSession session = request.getSession();
        String uri = request.getRequestURI();
        if (uri.startsWith("/management") && session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/user/login");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
