package geektime.demo.config;

import geektime.demo.converters.AgeBracketEnumConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dominiczhu
 * @date 2020/8/11 5:18 下午
 */
@Configuration
public class ConverterConfig {
    @Bean
    public ConversionServiceFactoryBean conversionServiceFactoryBean() {
        ConversionServiceFactoryBean factoryBean = new ConversionServiceFactoryBean();
        Set<Converter<?, ?>> set = new HashSet<>();
        set.add(new AgeBracketEnumConverter());
        factoryBean.setConverters(set);
        return factoryBean;
    }
}
