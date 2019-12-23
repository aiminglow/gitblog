package com.aiminglow.gitblog.controller.advice;

import com.aiminglow.gitblog.util.ResultGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Constraint;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.Set;

/**
 * @ClassName ControllerFieldCheckHandler
 * @Description 切面类，用来切入controller的方法，对字段校验失败后的ConstraintViolationException进行捕获，然后返回response。
 * 所以说Advice的类型为Exception Advice。
 * @Author aiminglow
 */
@ControllerAdvice
@Component
public class ControllerFieldCheckHandler {
    /**
     * @Description: hibernate 参数校验出错会抛出 ConstraintViolationException 异常
    在此方法中处理，将错误信息放在response中返回。
     * @Param:
     * @return:
     * @Author: aiminglow
     */
    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handle(ValidationException exception){
        StringBuilder errorInfo = new StringBuilder();
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException exc = (ConstraintViolationException) exception;
            Set<ConstraintViolation<?>> violationSet = exc.getConstraintViolations();

            for (ConstraintViolation<?> item: violationSet) {
                errorInfo = errorInfo.append(item.getMessage() + "|");
            }
        }
        if (!errorInfo.toString().equals(""))
            errorInfo.deleteCharAt(errorInfo.length() - 1);
        return ResultGenerator.genFailResult(errorInfo.toString());
    }
}
