package webflux.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;


/**
 * [优雅的自定义Spring Cloud Gateway全局异常处理](https://blog.csdn.net/xiyang_1990/article/details/108151317)
 * 只需要定义这个就可以了，原生默认的DefaultErrorWebExceptionHandler很难定制化，如果定制化了，例如我自己写的badGlobalErrorWebExceptionHandler，只能针对controller里的方法进行异常处理
 * 但如果异常是框架抛出的，例如访问了一个不存在的路径，此时是框架抛出的异常，badGlobalErrorWebExceptionHandler无法处理这种异常
 * 要想即让所有异常都被处理掉，那么就不能自己写异常处理handler，只需要修改ErrorAttributes就行了。
 */
@Component
@Slf4j
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  ErrorAttributeOptions options) {
        Map<String, Object> map = super.getErrorAttributes(request, options);
        Throwable error = this.getError(request);
        map.put("errorMessage",error.getMessage());
        map.put("status", HttpStatus.BAD_REQUEST.value());
        map.put("message", "username is required");
        return map;
    }

}