package toolkits.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @version 1.0
 * @title InterceptorDemo
 * @date 2021/8/11 上午11:05
 */
// 不可以加component，否则这个拦截器会全局生效
//@Component
public class InterceptorDemo implements RequestInterceptor {

    // 但是即使没有component，value注解也会生效
    @Value("${header-test}")
    private String h;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("header-test",h);
    }
}
