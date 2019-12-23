package com.aiminglow.gitblog.dao;

import com.aiminglow.gitblog.entity.User;
import com.aiminglow.gitblog.entity.UserToken;
import org.apache.ibatis.annotations.Param;


public interface MyUserMapper {
    public int updateUserByToken(@Param("userToken") UserToken userToken, @Param("user") User user);
}
