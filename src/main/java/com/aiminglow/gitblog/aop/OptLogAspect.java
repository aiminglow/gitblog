package com.aiminglow.gitblog.aop;

import com.aiminglow.gitblog.annotation.OptLog;
import com.aiminglow.gitblog.dao.OptLogMapper;
import com.aiminglow.gitblog.util.HttpContextUtils;
import com.aiminglow.gitblog.util.TimeUtil;
import com.aiminglow.gitblog.util.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;

/**
 * @ClassName OptLogAspect
 * @Description 注解@OptLog的切面类
 * @Author aiminglow
 */
@Aspect
@Component
public class OptLogAspect {
    @Autowired
    private OptLogMapper optLogMapper;

    @Pointcut("@annotation(com.aiminglow.gitblog.annotation.OptLog)")
    public void pointcut() { }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Exception{
        long beginTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = point.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
            // 记录发生异常的交易的日志
            long failExecuteTime = System.currentTimeMillis() - beginTime;
            failLog(point, failExecuteTime, e.getMessage());
            return result;
        }
        // 方法的执行时长（单位：毫秒）
        long executeTime = System.currentTimeMillis() - beginTime;

        // 记录正常结束的交易的日志
        successLog(point, executeTime);
        return result;
    }

    private com.aiminglow.gitblog.entity.OptLog getOptLog(ProceedingJoinPoint point, long executeTime) throws Exception{
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // OptLog的entity对象，需要记录入数据库
        com.aiminglow.gitblog.entity.OptLog optLog = new com.aiminglow.gitblog.entity.OptLog();
        optLog.setExecuteTime((short) executeTime);
        optLog.setCreateTime(TimeUtil.getCurrentTimeInt());

        // 获得OptLog注解
        OptLog logAnnotation = method.getAnnotation(OptLog.class);
        if (logAnnotation != null) {
            optLog.setOptName(logAnnotation.value());
        }

        // 执行的类名和方法名
        String className = point.getTarget().getClass().getName();
        String methodName = signature.getName();
        optLog.setMethod(className + "." + methodName);

        /**
         * 在之后的使用中，如果参数中包括了博客正文之类的信息，信息很长，这个字段肯定没办法存储，
         * 这就要增加@OptLog的属性和功能，从而面对复杂的项目了。
         */
        // 获得执行的方法的参数值
        Object[] args = point.getArgs();
        // 获得执行的方法的参数名
        LocalVariableTableParameterNameDiscoverer var = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = var.getParameterNames(method);
        if (args != null && paramNames != null) {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                params.append(paramNames[i] + "=" + args[i] + ", ");
            }
            if (params.length() >= 2) params.delete(params.length() - 2, params.length());
            optLog.setParams(params.toString());
        }

        // 获取session
        HttpSession session = HttpContextUtils.getSession();

        /**
         * opt_log表中request_uri，user_agent和error_msg三个字段最长1000，
         * 所以说我需要在哪里做一下这些字段长度的限制来避免数据插入时报错吗？要在那里进行限制呢？
         * 或许可以让数据库直接截取，然后在一般的日志中打印出来详细信息就可以了？
         *
         * 目前来看还是要使用strict mode，然后在程序中做字段长度的校验和裁剪
         */
        // 获取用户IP和user agent
        optLog.setRequestUri(WebUtils.getUri());
        optLog.setIp(WebUtils.getClientIp());
        optLog.setUserAgent(WebUtils.getUserAgent());

        // 获取用户id。这个表的用户id目前为外键，不能为空。
        // 而且暂时实现成必须是已登录用户才会记录日志，因为这里的userId是从session里面取到的。
        // TODO 以后也许可能会更改这部分设计，使得其适应没有userId的场景
        // TODO 这部分容易出错的逻辑也许应该放在最前面？
        Object userIdObject = session.getAttribute("userId");
        // 如果userId为空，这里抛出了异常，
        // TODO 那么整个请求能够顺利完成吗？controller已经执行了，用户能收到response吗？
        if (userIdObject == null)
            throw new Exception("session中userId为空，无法记录操作日志");

        if (! (userIdObject instanceof Number))
            throw new Exception("session中userId类型不正确，无法记录用户操作日志");

        Long userId = ((Number) userIdObject).longValue();
        optLog.setUserId(userId);

        return optLog;
    }

    // 记录正常结束的交易的日志
    private void successLog(ProceedingJoinPoint point, long executeTime) throws Exception{
        com.aiminglow.gitblog.entity.OptLog optLog = getOptLog(point, executeTime);
        // 设置操作状态为1-成功
        optLog.setLogStatus((byte) 1);
        optLogMapper.insertSelective(optLog);
    }

    // 记录发生异常的交易的日志
    private void failLog(ProceedingJoinPoint point, long executeTime, String errorMsg) throws Exception{
        com.aiminglow.gitblog.entity.OptLog optLog = getOptLog(point, executeTime);

        // 设置操作状态为-1-失败
        optLog.setLogStatus((byte) -1);
        optLog.setErrorMsg(errorMsg);
        optLogMapper.insertSelective(optLog);
    }
}
