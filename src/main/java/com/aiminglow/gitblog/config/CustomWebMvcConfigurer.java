package com.aiminglow.gitblog.config;

import com.aiminglow.gitblog.interceptor.UserLoginInterceptor;
import com.aiminglow.gitblog.interceptor.UserLoginedInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName CustomWebMvcConfigurer
 * @Description
 * @Author aiminglow
 */
@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private UserLoginInterceptor userLoginInterceptor;
    @Autowired
    private UserLoginedInterceptor userLoginedInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userLoginInterceptor).addPathPatterns("/**").excludePathPatterns("/user/**")
            .excludePathPatterns("/common/**");
        registry.addInterceptor(userLoginedInterceptor).addPathPatterns("/user/login");
    }
}
