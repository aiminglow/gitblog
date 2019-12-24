package com.aiminglow.gitblog.controller.user;

import com.aiminglow.gitblog.entity.User;
import com.aiminglow.gitblog.service.UserService;
import com.aiminglow.gitblog.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.*;

@Controller
@Validated
@RequestMapping("/user")
public class UserController {

    public static final String USER_NOT_NULL = "用户名不能为空";
    public static final String EMAIL_NOT_NULL = "邮箱不能为空";
    public static final String PWD_NOT_NULL = "密码不能为空";
    public static final String CODE_NOT_NULL = "验证码不能为空";

    public static final String USER_NOT_VALID = "用户名可以由中文，大小写英文字母，数字，-和下划线组成，长度4~16";
    public static final String EMAIL_NOT_VALID = "邮箱不符合规范";
    public static final String PWD_NOT_VALID = "密码必须包括至少一个大小写字母或数字，不能包含特殊字符，长度8~12";
    public static final String ENCODED_PWD_NOT_VALID = "密码必须为32位小写字母和数字组合";
    public static final String CODE_NOT_VALID = "验证码不符合规范";

    public static final String PWD_NOT_EQUALS = "两次输入密码不一致";
    public static final String CODE_NOT_CORRECT = "验证码不正确";

    @Autowired
    UserService userService;

    @GetMapping("/signup")
    public String signup() {
        return "user/signup";
    }

    /**
     * @Description: front end use ajax to check if email is deplicate; back end generate email and salt
     * @Param: []
     * @return: java.lang.String
     * @Author: aiminglow
     */
    @GetMapping("/check-dup-user-email")
    @ResponseBody
    public Result checkDuplicateEmail(@NotNull(message = EMAIL_NOT_NULL)
                                          @Email(message = EMAIL_NOT_VALID)
                                          @RequestParam String userEmail) {

        if (userService.emailHasRegister(userEmail)) {
            return ResultGenerator.genFailResult("邮箱已注册");
        }

        return ResultGenerator.genSuccessResult("邮箱可用");
    }

    @GetMapping("/check-dup-user-name")
    @ResponseBody
    public Result checkDuplicateUserName(@NotNull(message = USER_NOT_NULL)
                                             @Pattern(regexp = PatternUtil.USER_NAME_REGEX, message = USER_NOT_VALID)
                                             @RequestParam String userName) {

        if (userService.userNameHasRegister(userName)) {
            return ResultGenerator.genFailResult("用户名已被占用");
        } else {
            return ResultGenerator.genSuccessResult();
        }
    }

    @GetMapping("/verify-code")
    @ResponseBody
    public Result verifyCode(@NotNull(message = CODE_NOT_NULL)
                                 @Pattern(regexp = PatternUtil.VERIFY_CODE_REGEX, message = CODE_NOT_VALID)
                                 @RequestParam String code, HttpSession session) {
        String verifyCode = ((String) session.getAttribute("verifyCode")).toUpperCase();
        if (code.toUpperCase().equals(verifyCode)) {
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("验证码错误");
        }
    }

    @GetMapping("/verify-pwd")
    public Result verifyPassword(@NotNull(message = PWD_NOT_NULL)
                                 @Pattern(regexp = PatternUtil.ENCODED_PWD_REGEX, message = ENCODED_PWD_NOT_VALID)
                                 @RequestParam String pwd,
                                 @NotNull(message = "确认" + PWD_NOT_NULL)
                                 @Pattern(regexp = PatternUtil.ENCODED_PWD_REGEX, message = "确认" + ENCODED_PWD_NOT_VALID)
                                 @RequestParam String pwdConfirm) {
        if (!pwd.equals(pwdConfirm)) {
            return ResultGenerator.genFailResult("确认密码与密码不相同");
        }
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping(value = "/signup")
    @ResponseBody
    public Result signup(@RequestParam String userEmail, @RequestParam String userName,
                         @RequestParam String pwd, @RequestParam String pwdConfirm,
                         @RequestParam String code, HttpSession session) {
         /**
         如果想要省去下面这些步骤，最好的办法是前面的那些校验的步骤成功后，都给session里面设置一个key：value。
         key为某一个操作的名字。比如“verify-pwd”，value为Boolean值。session需要限制时间，用的时候全部做与运算就行了。
         如果与运算失败了，怎么办呢？让前端再发一遍“verify-pwd”还是自己直接调用？后者调用对应方法好一点。

         但是分析了一下，还是不行。攻击者可以先填入合法的输入，然后通过verify-pwd的校验，
         然后修改前端js，使得在发signup请求的时候使用非法字符不会报错，这时候如果我使用session保存的校验结果就会将错误的字符串在sql中执行。
         */
        Result result = verifyCode(code, session);
        if (!result.isSuccessResult()) {
            return result;
        }
        result = checkDuplicateEmail(userEmail);
        if (!result.isSuccessResult()) {
            return result;
        }
        result = checkDuplicateUserName(userName);
        if (!result.isSuccessResult()) {
            return result;
        }
        result = verifyPassword(pwd, pwdConfirm);
        if (!result.isSuccessResult()) {
            return result;
        }

         /**
         这里使用到了checkDuplicateEmail方法中set进session的user，这个对象也存进了数据库
         按照signup方法的实现，checkDuplicateEmail方法还会在执行一遍，session也就会被再次更新一遍
         上面“session重复更新”可能都不重要，要想节约资源，我们需要考虑“真的有必要在检查邮箱的时候就插入数据库吗？”
         当初是因为在插入password之前先生成salt才提前生成数据库的记录的，现在不需要了。
         而且，也许未来可能会用到缓存，所以如果要优化查询的效率问题应该先避免在session这个方面优化，而是先在缓存方面优化
         */
        User user = new User();
        user.setUserEmail(userEmail);
        user.setUserName(userName);
        user.setUserPassword(pwd);
        if (userService.signup(user)) {
            return ResultGenerator.genSuccessResult("注册成功，请在您的邮箱中点击链接确认，确认后即可登录");
        }
        return ResultGenerator.genFailResult("注册失败");
    }

    /**
     * @Description: verify email after signup
     * @Param: []
     * @return: java.lang.String
     * @Author: aiminglow
     */
    @GetMapping("/verify/{token}")
    @ResponseBody
    public Result verify(@NotNull(message = "token不能为空") @PathVariable String token) {
        if (userService.verifySignUpToken(token))
            return ResultGenerator.genSuccessResult("验证邮箱成功！");
        return ResultGenerator.genFailResult("验证失败！");
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    /**
     * @Description: 之前这个方法主要是为了返回盐值，但是现在不需要了。现在主要是为了检查userEmail是否注册过，
     * 如果没有注册过需要提示用户先注册。所以方法名字要改，需要的userService之前已经写过了。
     * 这个方法的主要作用是“改善用户体验，如果用户输入错误了，在登录操作之前提醒用户”。所以这个方法在“/login”请求的时候需要
     * 再执行一遍，防止非法字符。
     *
     * 【2019年12月23日】这里如果提供了改善用户体验的设计，开放了“一个邮箱是否已经注册”的接口，黑客可能会利用接口获得已经注册的用户，然后再调用login
     * 请求登录。提供这个接口，减少了对方的试错成本，不可取，所以删去这个接口。
     * @Param: [email, cookie]
     * @return: com.aiminglow.gitblog.util.Result
     * @Author: aiminglow
     */
    /**@GetMapping("/email-can-login")
    @ResponseBody
    public Result emailCanLogin(@NotNull(message = EMAIL_NOT_NULL)
                                       @Email(message = EMAIL_NOT_VALID)
                                       String email, HttpCookie cookie) {
        return ResultGenerator.genFailResult("该邮箱尚未注册，请注册后登录！");
    }*/

    @PostMapping(value = "/login")
    @ResponseBody
    public ResponseEntity<Result> login(@NotNull(message = EMAIL_NOT_NULL)
                                    @Email(message = EMAIL_NOT_VALID)
                                    String userEmail,
                                        @NotNull(message = PWD_NOT_NULL)
                                    @Pattern(regexp = PatternUtil.ENCODED_PWD_REGEX, message = ENCODED_PWD_NOT_VALID)
                                    String pwd, HttpSession session) {
        User user = userService.login(userEmail, pwd);
        if (user == null || user.getUserId() == null)
            return new ResponseEntity<Result>(ResultGenerator.genFailResult("邮箱或密码错误"), HttpStatus.BAD_REQUEST);

        // 目前采用“session不能因为用户的操作刷新”的模式，所以设置了server.servlet.session.cookie.max-age=604800，最长时间为7天
        // TODO 之后写一个脚本，定时清理redis里面「creationTime+maxInactiveInterval>现在时间戳」的session记录
        session.setAttribute("userId", user.getUserId());
        session.setMaxInactiveInterval(86400 * 7);

        return new ResponseEntity<Result>(ResultGenerator.genSuccessResult(), HttpStatus.OK);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null)
            session.removeAttribute("userId");
        return "user/login";
    }
}
