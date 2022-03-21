package geektime.demo.config;

import geektime.demo.converters.AgeBracketEnumConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title WebConfig
 * @date 2021/9/3 下午2:05
 */
@Configuration
public class WebConfig  implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new AgeBracketEnumConverter());
    }

}
