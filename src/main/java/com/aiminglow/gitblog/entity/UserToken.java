package com.aiminglow.gitblog.entity;

import java.io.Serializable;

public class UserToken implements Serializable {
    private Long tokenId;

    private String token;

    private Long userId;

    private Byte tokenType;

    private Integer createTime;

    private Integer resetTime;

    private Short effectiveTime;

    private static final long serialVersionUID = 1L;

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Byte getTokenType() {
        return tokenType;
    }

    public void setTokenType(Byte tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Integer getResetTime() {
        return resetTime;
    }

    public void setResetTime(Integer resetTime) {
        this.resetTime = resetTime;
    }

    public Short getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(Short effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", tokenId=").append(tokenId);
        sb.append(", token=").append(token);
        sb.append(", userId=").append(userId);
        sb.append(", tokenType=").append(tokenType);
        sb.append(", createTime=").append(createTime);
        sb.append(", resetTime=").append(resetTime);
        sb.append(", effectiveTime=").append(effectiveTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}