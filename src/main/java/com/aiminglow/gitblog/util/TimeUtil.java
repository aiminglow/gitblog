package com.aiminglow.gitblog.util;

import java.util.Date;

/**
 * @ClassName TimeUtil
 * @Description 时间工具类
 * @Author aiminglow
 */
public class TimeUtil {
    /**
     * @Description: 生成当前时间戳，单位为秒
     * @Param: []
     * @return: java.lang.Integer
     * @Author: aiminglow
     */
    public static Integer getCurrentTimeInt() {
        Integer timeInt = (int) ((new Date()).getTime() / 1000);
        return timeInt;
    }
}
