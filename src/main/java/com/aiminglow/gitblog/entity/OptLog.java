package com.aiminglow.gitblog.entity;

import java.io.Serializable;

public class OptLog implements Serializable {
    private Long logId;

    private String optName;

    private Long userId;

    private String requestUri;

    private String method;

    private String params;

    private String ip;

    private String userAgent;

    private String errorMsg;

    private Integer createTime;

    private Short executeTime;

    private Byte logStatus;

    private static final long serialVersionUID = 1L;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getOptName() {
        return optName;
    }

    public void setOptName(String optName) {
        this.optName = optName == null ? null : optName.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri == null ? null : requestUri.trim();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method == null ? null : method.trim();
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params == null ? null : params.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent == null ? null : userAgent.trim();
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg == null ? null : errorMsg.trim();
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Short getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Short executeTime) {
        this.executeTime = executeTime;
    }

    public Byte getLogStatus() {
        return logStatus;
    }

    public void setLogStatus(Byte logStatus) {
        this.logStatus = logStatus;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logId=").append(logId);
        sb.append(", optName=").append(optName);
        sb.append(", userId=").append(userId);
        sb.append(", requestUri=").append(requestUri);
        sb.append(", method=").append(method);
        sb.append(", params=").append(params);
        sb.append(", ip=").append(ip);
        sb.append(", userAgent=").append(userAgent);
        sb.append(", errorMsg=").append(errorMsg);
        sb.append(", createTime=").append(createTime);
        sb.append(", executeTime=").append(executeTime);
        sb.append(", logStatus=").append(logStatus);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}