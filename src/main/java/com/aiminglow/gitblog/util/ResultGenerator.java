package com.aiminglow.gitblog.util;

import org.springframework.util.StringUtils;

/**
 * @ClassName ResultGenerator
 * @Description Result对象生成器
 * @Author aiminglow
 */
public class ResultGenerator {
    private static final int RESULT_CODE_SUCCESS = 1;
    private static final int RESULT_CODE_FAIL = 0;
    private static final String MESSAGE_SUCCESS = "SUCCESS";
    private static final String MESSAGE_FAIL = "FAIL";

    public static Result genResult() {
        return new Result();
    }

    public static Result genResult(int code, String message) {
        Result result = new Result();
        result.setData(code);
        result.setMessage(message);
        return result;
    }

    public static Result genSuccessResult() {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(MESSAGE_SUCCESS);
        return result;
    }

    public static Result genSuccessResult(String message) {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(message);
        return result;
    }

    public static Result genSuccessResult(Object data) {
        Result result = genSuccessResult();
        result.setData(data);
        return result;
    }

    public static Result genSuccessResult(String message, Object data) {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_SUCCESS);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static Result genFailResult(String message) {
        Result result = new Result();
        result.setResultCode(RESULT_CODE_FAIL);
        if (StringUtils.isEmpty(message)) {
            result.setMessage(MESSAGE_FAIL);
        } else {
            result.setMessage(message);
        }
        return result;
    }


}
