package com.aiminglow.gitblog.controller.common;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName NotFoundController
 * @Description
 * @Author aiminglow
 */
@Controller
public class NotFoundController implements ErrorController {
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * @Description: 按照HttpStatus状态码的不同，返回不同的页面
     * 原理：当url的path寻找不到controller的mapping之后，也寻找不到静态资源时，
     * request对象的requestDispatcherPath就设定为"/error"，然后就会redirect到这个path。
     * 我们只需要定义一个"/error"的controller mapping，然后按照状态码返回页面或者返回对象就行了。
     *
     * 这里暂时只返回html，以后如果有必要再配置错误信息显示。
     * @Param: [request]
     * @return: java.lang.String
     * @Author: aiminglow
     */
    @GetMapping(value = "/error", produces = "text/html")
    public String errorHtml(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.BAD_REQUEST) {
            return "error/400";
        } else if (status == HttpStatus.NOT_FOUND) {
            return "error/404";
        } else {
            return "error/500";
        }
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            try {
                return HttpStatus.valueOf(status);
            } catch (Exception e) {

            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
