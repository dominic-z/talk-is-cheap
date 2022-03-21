package demo.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HelloServiceAspect
 * @date 2021/9/14 下午4:59
 */
@Component
@Aspect
public class HelloServiceAspect {

    @Pointcut("execution(public * demo.services.HelloService.sayHelloForSelf(..))")
    public void sayHelloForSelfPC(){}

    @Around("demo.aop.HelloServiceAspect.sayHelloForSelfPC()")
    public Object sayHelloAOP(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] sayHelloAOP start");
        Object retVal = pjp.proceed();
        System.out.println(pjp.getSignature());
        // stop stopwatch
        System.out.println("[aop] sayHelloAOP stop");
        return retVal;
    }

    @Pointcut("execution(public * demo.services.HelloService.callSelf(..))")
    public void callSelfPC(){}
    @Around("demo.aop.HelloServiceAspect.callSelfPC()")
    public Object callSelfAOP(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] callSelfAOP start");
        Object retVal = pjp.proceed();
        System.out.println(pjp.getSignature());
        // stop stopwatch
        System.out.println("[aop] callSelfAOP stop");
        return retVal;
    }
}
