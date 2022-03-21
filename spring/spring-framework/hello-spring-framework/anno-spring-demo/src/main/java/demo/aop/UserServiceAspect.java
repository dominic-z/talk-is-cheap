package demo.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @version 1.0
 * @title UserServiceAspect
 * @date 2021/9/14 下午5:16
 */
@Aspect
@Component
public class UserServiceAspect {
    @Before("execution(public * demo.service.UserService.*(..))")
    public void doAccessCheck() {
        System.err.println("[Before] do access check...");
    }
}
