package org.talk.is.cheap.hello.spring.openfeign.frontend.client.interceptor;

import feign.*;
import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.hello.spring.openfeign.common.message.GenericData;
import org.talk.is.cheap.hello.spring.openfeign.frontend.client.HijClient;

import java.util.Collection;
import java.util.Map;


/**
 * ResponseInterceptor是尝试出来的，我感觉既然有RequestInterceptor，就一定有ResponseInterceptor
 *
 * 注意，这里的异常都不会被抛出而是会被吞掉！！
 */
@Slf4j
public class HijClientInterceptor implements RequestInterceptor, ResponseInterceptor {

    /**
     * 请求拦截器
     *
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String path = requestTemplate.path();
        byte[] body = requestTemplate.body();
        String url = requestTemplate.url();
        Target<HijClient> target = (Target<HijClient>) requestTemplate.feignTarget();
        requestTemplate.header("interceptor", "header");

//        强行改变请求体
//        requestTemplate.body("{\"code\":1,\"data\":\"interceptor\",\"msg\":null}");
//        无法修改uri，拦截器不支持将请求路径修改为绝对路径，代码中有拦截，
//        requestTemplate.uri("http://localhost:8081")
        log.info("url:{} ,path: {}, body: {}, target:{}", url, path, new String(body), target);
    }

    @Override
    public Object intercept(InvocationContext invocationContext, Chain chain) throws Exception {
        Response response = invocationContext.response();

        Map<String, Collection<String>> headers = response.headers();
        Response.Body body = response.body();
        log.info("response class: {}, headers:{}, body:{}", response.getClass(), headers, body);
//        对response进行处理直接返回
//        return chain.next(invocationContext);


        // 根据豆包对拦截器的回答https://www.doubao.com/thread/w2735260de7e2be57
        // 以下内容均为摸索出来的，估摸着应该有内置的Decoder，一看源码，果然有，还有这种各样的
//        注意，response对象都有个特点，就是只能read一次，所以以任何方式触发了一次read导致response对象close了，再次读取都会报错
//        Decoder decoder = invocationContext.decoder();
//        String decodeBodyStr = (String) new StringDecoder().decode(response, String.class);
//        log.info("decodeBodyStr: {}", decodeBodyStr);
//        byte[] decodeBodyBytes = (byte[]) new Decoder.Default().decode(response, byte[].class);
//        log.info("decodeBodyBytes: {}",new String(decodeBodyBytes,StandardCharsets.UTF_8));

//        直接反序列化，获得的就是反序列化并进行类型转换后的responseData
//        Object proceed = invocationContext.proceed();
//        log.info("class: {}, content: {}", proceed.getClass(), proceed);

//        你会发现这就是个反序列化后的responseBody对象，在这里也就是GenericData，也就是说需要返回response的body对象，在这里就是GenericData。
//        Object next = chain.next(invocationContext);
//        log.info("next class {}, next is: {}", next.getClass(), next);


//        根据上述发现，对响应对象进行处理
        GenericData<?> proceed = (GenericData<?>) invocationContext.proceed();
        proceed.setMsg(String.format("%s,%s", proceed.getMsg(), "interceptor"));
        return proceed;
    }
}
