package com.aiminglow.gitblog.service;

import com.aiminglow.gitblog.dao.MyUserMapper;
import com.aiminglow.gitblog.dao.UserMapper;
import com.aiminglow.gitblog.dao.UserTokenMapper;
import com.aiminglow.gitblog.entity.User;
import com.aiminglow.gitblog.entity.UserExample;
import com.aiminglow.gitblog.entity.UserToken;
import com.aiminglow.gitblog.entity.UserTokenExample;
import com.aiminglow.gitblog.util.StringUtil;
import com.aiminglow.gitblog.util.TimeUtil;
import com.aiminglow.gitblog.util.UserPwdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName UserService
 * @Description TODO
 * @Author aiminglow
 */
@Service
public class UserService {

    // Token 有效时间 86400秒 == 一天
    public static final short TOKEN_EFFECTIVE_TIME = (short) 86400;
    public static final String EMAIL_SUBJECT = "您已注册成功！";
    public static final String EMAIL_CONTENT = "您好：\n    您已注册成功，请点击 %s 完成注册。完成后即可登录！";
    public static final String URL_PREFIX = "http://www.aiminglow.me/user/verify/";

    @Autowired
    private MyUserMapper myUserMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserTokenMapper userTokenMapper;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserPwdEncoder userPwdEncoder;

    public boolean emailHasRegister(String email) {
        UserExample userExample = new UserExample();
        /**
         如果一个email已经注册过，但是删除了账户（user_status为-1），那么允许这个email继续注册，但是不能看到原来账户的信息。
         所以需要新创建一条user表记录，分配新的user_id。如果update原来的user记录，那么其他表的外键user_id和原来的user关联就有可能
         看到原来的用户信息。虽然其他信息的表应该也把status改为“已删除”状态，但是为了把前后的信息分的清清楚楚，还是新建一个user比较好。

         哪怕按上面的情况没有发生，如果user表有一些基本信息，比如签名。
         新用户注册并没有写，但是使用原来的user记录的话，现在的用户就会看到原来的签名。

         所以下面的查询条件需要把email相同，但已经删除的账户排除在外

         ※ 也因此，所有查询user和更新user的时候，条件中必须有andUserStatusNotEqualTo((byte) -1)。如果发现where条件中只使用了
         andUserEmailEqualTo(email)的情况，要加上上面的条件（除了一些要求status为其他状态的情况，比如等于1,0,-2的情况）。
         */
        userExample.or().andUserEmailEqualTo(email).andUserStatusNotEqualTo((byte) -1);

        List<User> userList = userMapper.selectByExample(userExample);
        if (null == userList || userList.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @Description: 这个方法是以前在用户填写了email之后就马上创建一条数据库记录的，但是现在觉得不应该在“只填写了email”的情况下就创建记录
     * 所以这个方法就被废弃了。暂时不删除这个方法，先留着，也许以后还会改回来。
     * @Param: [userEmail]
     * @return: java.util.List<com.aiminglow.gitblog.entity.User>
     * @Author: aiminglow
     */
    /**@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public List<User> insertEmail(String userEmail) {
        UserExample userExample = new UserExample();
        userExample.or().andUserEmailEqualTo(userEmail);
        List<User> userList = userMapper.selectByExample(userExample);
        if (null != userList && !userList.isEmpty()) {
            return userList;
        }

        User user = new User();
        user.setUserEmail(userEmail);
        user.setUserStatus((byte) -2);
        Integer time = TimeUtil.getCurrentTimeInt();
        user.setCreateTime(time);
        user.setLastModTime(time);
        userMapper.insertSelective(user);

        List<User> userList1 = new ArrayList<User>(1);
        userList1.add(user);
        return userList1;
    }*/

    public boolean userNameHasRegister(String userName) {
        /**
         * userName不能重复，但是表中user_name并没有使用unique key。
         * 因为如果原来一个用户用过某个用户名，但是已经注销了账户的话，新用户则可以使用这个用户名。
         */
        UserExample userExample = new UserExample();
        userExample.or().andUserNameEqualTo(userName).andUserStatusNotEqualTo((byte) -1);
        List<User> userList = userMapper.selectByExample(userExample);
        if (null == userList || userList.isEmpty()) {
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public boolean signup(User user) {
        user.setCreateTime(TimeUtil.getCurrentTimeInt());
        user.setUserStatus((byte) -2);
        // encode pwd
        PasswordEncoder passwordEncoder = userPwdEncoder.createEncoder();
        String encodedPwd = passwordEncoder.encode(user.getUserPassword());
        user.setUserPassword(encodedPwd);

        int count = userMapper.insertSelective(user);
        if (count < 1) return false;

        // 现在不写mybatis的复杂sql，所以需要单独的sql来查询刚才插入的user的id，来给插入token表的记录设置外键
        // 未来改成JAP+MyBatis之后，下面的逻辑使用MyBatis写成一句复杂sql
        // 现在看来，这一句其实是可写可不写的。不过为了复习一下MyBatis在xml里面对参数的使用，之后可以实现一下。
        UserExample example = new UserExample();
        UserExample.Criteria criteria = example.createCriteria();
        criteria.andUserEmailEqualTo(user.getUserEmail()).andUserStatusNotEqualTo((byte) -1);
        List<User> userList = userMapper.selectByExample(example);
        if (userList == null || userList.isEmpty()) return false;
        User userWithId = userList.get(0);

        /*
        给token表插入需要验证的token的记录
         */
        String token = StringUtil.genRandomStringByLength(36);
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setCreateTime(TimeUtil.getCurrentTimeInt());
        userToken.setUserId(userWithId.getUserId());
        userToken.setTokenType((byte) 11);
        userToken.setEffectiveTime(TOKEN_EFFECTIVE_TIME);
        int insertCount = userTokenMapper.insertSelective(userToken);
        if (insertCount < 1) return false;

        /*
        给用户发送确认邮件，邮件中有携带token的url，点击之后即可注册
         */
        emailService.sendEmail(user.getUserEmail(), EMAIL_SUBJECT, String.format(EMAIL_CONTENT, URL_PREFIX + token));

        return true;
    }

    /*
    目前验证token就不要求必须在指定时间内验证了，也就是说不检查token有效期了
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public boolean verifySignUpToken(String token){
        // 修改token状态 从 11-注册，未使用过 到 12-注册，使用过
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setResetTime(TimeUtil.getCurrentTimeInt());
        userToken.setTokenType((byte) 12);

        UserTokenExample example = new UserTokenExample();
        example.or().andTokenEqualTo(token).andTokenTypeEqualTo((byte) 11);

        int count = userTokenMapper.updateByExampleSelective(userToken, example);
        if (count < 1)
            return false;

        // 修改user的状态，从 -3-已注册，但未验证 到 1-正常
        /*
        这里完全可以写成一个嵌套的sql，但是为了测试MyBatis的新功能，就先使用这种效率低又非常难用的写法
        之后改成一句sql
         */
        /*UserTokenExample selectExample = new UserTokenExample();
        selectExample.or().andTokenTypeEqualTo((byte) 12).andTokenEqualTo(token);
        List<UserToken> userTokenList = userTokenMapper.selectByExample(selectExample);
        if (userTokenList == null || userTokenList.size() < 0) return false;
        UserToken tokenWithId = userTokenList.get(0);

        User user = new User();
        user.setUserStatus((byte) 1);
        user.setUserId(tokenWithId.getUserId());
        int countUser = userMapper.updateByPrimaryKeySelective(user);
        if (countUser != 1) return false;*/

        // 修改后写法
        /**
         * 修改了写法之后，service代码变的简洁很多，复杂的代码写在了mybatis的xml里面。
         * 但是性能上，一个嵌套的sql会比两次查询的sql好吗？
         * 1. 前者查询tokenWithId时，首先使用token字段的辅助索引，然后回表查全部字段。然后用userId更新user，这在查询的时候只需要主键索引。
         * 2. 后者在token表中查询user_id的时候，也使用到同样的辅助索引，需要的user_id在主键索引中，所以也需要回表。后面的更新操作都是一样的。
         * - 但是如果建立一个token和user_id的辅助索引，那么就会减少一次回表，效率就会提高。
         * - 后者的整个操作中间没有在java代码中的操作，这能节约多少时间？这里是指相比其他方面的优化，这一种优化的效果有多少？
         *
         * 我的结论：
         * 1. 前期写应用的时候完全可以用前者，虽然service里写的很麻烦，但是也没有把复杂的东西藏在xml中。
         * 2. 如果这句sql需要优化，会增加一些联合索引来增强性能，那么就必须在xml中写sql，通过联合索引减少回表次数。
         *
         * 问题：
         * 1. 两次查token表都得回表的话，查全部字段和部分字段，性能相差大吗？
         * - 如果相差很大的话，那么以后那些简单的查询语句都要优化一下，只查询需要的字段了。
         */
        User user = new User();
        user.setUserStatus((byte) 1);
        int countUser = myUserMapper.updateUserByToken(userToken, user);
        if (countUser != 1) return false;

        return true;
    }

    public User login(String userEmail, String pwd) {
        UserExample userExample = new UserExample();
        /**
         * 用户状态为“-1-已删除; -2-注册了，但未验证邮箱”的，不能登录。
         * 为“1-正常（注册，且验证邮箱后）； 0-忘记密码，待重置； ”的可以登录。也就是说，就算是忘记密码，如果密码正确也可以登录。
         */
        List<Byte> statusList = new ArrayList<>(2);
        statusList.add((byte) 1);
        statusList.add((byte) 0);
        userExample.or().andUserEmailEqualTo(userEmail).andUserStatusIn(statusList);
        List<User> userList = userMapper.selectByExample(userExample);
        if (userList == null || userList.size() < 1)
            return null;

        User user = userList.get(0);
        PasswordEncoder encoder = userPwdEncoder.createEncoder();
        if (encoder.matches(pwd, user.getUserPassword()))
            return user;

        return null;
    }
}
