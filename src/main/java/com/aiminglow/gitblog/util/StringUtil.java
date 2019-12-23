package com.aiminglow.gitblog.util;

/**
 * @ClassName StringUtil
 * @Description TODO
 * @Author aiminglow
 */
public class StringUtil {
    private static final String SOURCE_STRING_62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                                   + "0123456789"
                                                   + "abcdefghijklmnopqrstuvxyz";

    /**
     * @Description: 生成对应长度的字符串，字符为大小写字符和数字
     * @Param: [length]
     * @return: java.lang.String
     * @Author: aiminglow
     */
    public static String genRandomStringByLength(int length) {
        StringBuffer sb = new StringBuffer(length);

        for(int i = 0; i < length; i++) {
            int index = (int) (Math.random() * SOURCE_STRING_62.length());
            sb.append(SOURCE_STRING_62.charAt(index));
        }
        return sb.toString();
    }
}
