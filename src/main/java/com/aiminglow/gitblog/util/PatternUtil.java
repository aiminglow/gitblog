package com.aiminglow.gitblog.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName PatternUtil
 * @Description 正则匹配工具类
 * 目前不是用这个类中的方法了，因为已经有hibernate的validator了
 * @Author aiminglow
 */
public class PatternUtil {

    // 用户名正则：用户名可以由中文，大小写英文字母，数字，-和下划线组成，长度4~16
    public static final String USER_NAME_REGEX = "^[\\u4E00-\\u9FA5a-zA-Z0-9_-]{4,16}$";
    // 密码正则：密码必须包括至少一个大小写字母或数字，不能包含特殊字符，长度8~12
    public static final String PWD_REGEX = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,20}$";
    public static final String MD5_32LEN_LOWEERCASE_REGEX = "^[a-z0-9]{32}$";
    public static final String ENCODED_PWD_REGEX = MD5_32LEN_LOWEERCASE_REGEX;
    public static final String VERIFY_CODE_REGEX = "^[a-zA-Z0-9]{4}$";

    public static boolean isEmail(String emailStr) {
        String regex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(emailStr);
        return matcher.matches();
    }

    public  static boolean isPassword(String pwdStr) {
        String regex = PWD_REGEX;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pwdStr);
        return matcher.matches();
    }
}
