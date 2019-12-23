package com.aiminglow.gitblog.util;

import java.io.Serializable;

/**
 * @ClassName Result
 * @Description 作为response的body返回给用户
 * TODO 这个类的resultCode和isSuccessResult可能没有必要，所以以后要考虑这些返回的信息。
 * @Author aiminglow
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 2019092224L;
    private int resultCode;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccessResult() {
        if (this.getResultCode() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Result{" +
                "resultCode=" + resultCode +
                ", message=" + message +
                ", data=" + data +
                '}';
    }
}
