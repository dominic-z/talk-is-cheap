package demo.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/12 6:48 下午
 */
@Aspect
@Component
public class MyAspect {
    @Pointcut("execution(public * demo.controllers.HelloController.he*o*(..))")
    public void plainPointCut(){}

    @Around("demo.aop.MyAspect.plainPointCut()")
    public Object doInAspectWithPlainPt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] plain pt start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] plain pt stop");
        return retVal;
    }


    @Pointcut("within(demo.controllers.HelloController)")
    public void withinPointCut(){}

    @Around("demo.aop.MyAspect.withinPointCut()")
    public Object doInAspectWithWithinPt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] within start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] within stop");
        return retVal;
    }

    @Pointcut("target(demo.controllers.HelloController)")
    public void target1PointCut(){}
    @Around("demo.aop.MyAspect.target1PointCut()")
    public Object doInAspectWithTarget1Pt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] target1 start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] target1 stop");
        return retVal;
    }


    @Pointcut("target(demo.controllers.FooController)")
    public void target2PointCut(){}
    @Around("demo.aop.MyAspect.target2PointCut()")
    public Object doInAspectWithTarget2Pt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] target2 start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] target2 stop");
        return retVal;
    }

    @Pointcut("target(demo.controllers.SupperController)")
    public void target3PointCut(){}
    @Around("demo.aop.MyAspect.target3PointCut()")
    public Object doInAspectWithTarget3Pt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] target3 start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] target3 stop");
        return retVal;
    }


    @Pointcut("this(demo.controllers.HelloController)")
    public void this1PointCut(){}
    @Around("demo.aop.MyAspect.this1PointCut()")
    public Object doInAspectWithThis1Pt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] this1 start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] this1 stop");
        return retVal;
    }


    @Pointcut("this(demo.controllers.FooController)")
    public void this2PointCut(){}
    @Around("demo.aop.MyAspect.this2PointCut()")
    public Object doInAspectWithThis2Pt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] this2 start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] this2 stop");
        return retVal;
    }

    @Pointcut("this(demo.controllers.SupperController)")
    public void this3PointCut(){}
    @Around("demo.aop.MyAspect.this3PointCut()")
    public Object doInAspectWithThis3Pt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] this3 start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] this3 stop");
        return retVal;
    }
//
//    @Pointcut("@target(org.springframework.web.bind.annotation.RestController)")
//    public void atTargetPointCut(){}
//    @Around("demo.aop.MyAspect.atTargetPointCut()")
//    public Object doInAspectWithAtTargetPt(ProceedingJoinPoint pjp) throws Throwable{
//        // start stopwatch
//        System.out.println("@target start");
//        Object retVal = pjp.proceed();
//        // stop stopwatch
//        System.out.println("@target stop");
//        return retVal;
//    }
//    这个会报错，未解决


    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void atWithinPointCut(){}
    @Around("demo.aop.MyAspect.atWithinPointCut()")
    public Object doInAspectWithAtWithinPt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] @within start");
        Object retVal = pjp.proceed();
        // stop stopwatch
        System.out.println("[aop] @within stop");
        return retVal;
    }


    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")//对有这个注解的方法进行aop
    public void atAnnoPointCut(){}
    @Around("demo.aop.MyAspect.atAnnoPointCut()")
    public Object doInAspectWithAtAnnoPt(ProceedingJoinPoint pjp) throws Throwable{
        // start stopwatch
        System.out.println("[aop] @Anno start");
        Object retVal = pjp.proceed();
        System.out.println(pjp.getSignature());
        // stop stopwatch
        System.out.println("[aop] @Anno stop");
        return retVal;
    }
}
