package com.aiminglow.gitblog.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @ClassName UserPwdEncoder
 * @Description 加密工具类
 * @Author aiminglow
 */
@Component
public final class UserPwdEncoder {
    @Autowired
    private Environment env;

    /**
     * @Description: 生成用于加密用户密码的encoder
     * @Param: []
     * @return: org.springframework.security.crypto.password.PasswordEncoder
     * @Author: aiminglow
     */
    public PasswordEncoder createEncoder() {
        // 这里的hashWidth选择128，生成的String长度为48
        PasswordEncoder encoder = new Pbkdf2PasswordEncoder(env.getProperty("pwd.secret"), 100, 128);
        ((Pbkdf2PasswordEncoder) encoder).setAlgorithm(Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
        return encoder;
    }
}
