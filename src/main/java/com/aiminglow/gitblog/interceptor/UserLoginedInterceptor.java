package com.aiminglow.gitblog.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @ClassName UserLoginedInterceptor
 * @Description 用户已登录状态 拦截器：如果已经登录，前往登录页面的话，直接导向用户主页或者其他页面
 * @Author aiminglow
 */
@Component
public class UserLoginedInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        HttpSession session = request.getSession();
        String uri = request.getRequestURI();
        if (session.getAttribute("userId") != null) {
            // 这里具体要导向哪个页面还不知道，因为有一部分表还都没有设计，TODO 之后做完设计这里需要改
            response.sendRedirect(request.getContextPath() + "/user/management");
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
