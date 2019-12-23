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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
                                    String pwd, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.login(userEmail, pwd);
        if (user == null || user.getUserId() == null)
            return new ResponseEntity<Result>(ResultGenerator.genFailResult("邮箱或密码错误"), HttpStatus.BAD_REQUEST);

        // 用户第一次访问网站，没有cookie，也就没有SESSION，那么我这里的session是否为null呢？
        // 或者，虽然不为null，但是set-cookie中，value的值可能还没有产生呢？
        // 没有cookie的情况postman好像还需要配置一下，不知道是否麻烦，TODO 但是之后不管怎么样都得测试一下上面这种情况。
        session.setAttribute("userId", user.getUserId());
        session.setMaxInactiveInterval(86400 * 7);

         /**
          设置浏览器的session时间，好像直接通过上面的setMaxInactiveInterval就行了，不需要下面这种操作了。
          但是session的path，安全性等等怎么设置呢？
          刚才测试之后，发现postman的set-cookie里面也没有设置时长，也就是说只有redis里面的更新了。

          ※ 但是或许浏览器的SESSION根本不需要有效时间，浏览器就一直保存着，一直使用，某一次使用服务端发现已经过期了，【？】应该在controller
          之前已经设置了新的session，也就是说使用拦截器拿到SESSION的值去redis里面查不到了，这时候拦截器也就要提醒用户重新登录了。
          而新的SESSION在set-cookie再给浏览器设置回去，就是这么方便。
          - 是否在controller之前就设置了session？这个问题可以通过debug来确定
            - 测试之后，发现debug到controller的方法时，session还没有产生。
          - 不过这也不影响上面的主要分析，也就是说SESSION没有必要在浏览器设置max-age，如果redis查不到的话就提醒登录。这时候返回给浏览器的response
          也会设置新的session的。
            - 但是还是要寻找一下设置session安全性的方法，不然不安全。
            - server.servlet.session.cookie.secure=true可以设置，还可以设置其他cookie的属性。
                - 这种方法只设置了浏览器端的数据，redis的市场还是默认的1800，需要在RedisSessionConfig类中设置默认时间，或者手动在代码里面设置。
          - 最后发现使用server.servlet.session.cookie.max-age=1800可以设置浏览器的session的max-age，但是不知道还有没有其他在java代码中的写法。

          总体来看，浏览器端的时间参数好像并不是很重要，因为服务端查不到的话还是需要在response中重新设置。有的作用可能就是在用户正常使用的情况下，
          浏览器可以直接删掉过期的cookie（session），然后就不发送这部分数据了。客户端如果在做权限校验，那么直接拿不到值，直接给用户报错，效率更高一些。
          - 也就是说session的max-age对“防止攻击”用处不大，因为“防止攻击”的重点在服务器。
          但是对提高效率还是有点用的，因为过期的时候，可以减少一次对redis的访问。

          TODO 这部分内容之后要记录下来，防止忘记。这种问题也应该写在“设计用户登录功能，不能忘记考虑的问题”的checklist里面。
          */

        return new ResponseEntity<Result>(ResultGenerator.genSuccessResult(), HttpStatus.OK);
    }

    @GetMapping("/logout")
    public String logout() {
        return "user/login";
    }
}
