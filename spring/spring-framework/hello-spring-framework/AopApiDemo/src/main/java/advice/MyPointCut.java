package advice;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import service.BlogService;

import java.lang.reflect.Method;

public class MyPointCut extends StaticMethodMatcherPointcut {
    @Override
    public boolean matches(Method method, Class<?> aClass) {
        if (BlogService.class.isAssignableFrom(aClass))
            return method.getParameterCount() != 0;
        return false;
    }
}
