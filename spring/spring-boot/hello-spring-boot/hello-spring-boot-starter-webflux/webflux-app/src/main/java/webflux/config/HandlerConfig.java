package webflux.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebExceptionHandler;

@Configuration
public class HandlerConfig {
//    public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers, ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
//        DefaultErrorWebExceptionHandler exceptionHandler = new GlobalErrorWebExceptionHandler(errorAttributes, webProperties.getResources(), this.serverProperties.getError(), applicationContext);
//        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
//        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
//        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
//        return exceptionHandler;
//    }
}