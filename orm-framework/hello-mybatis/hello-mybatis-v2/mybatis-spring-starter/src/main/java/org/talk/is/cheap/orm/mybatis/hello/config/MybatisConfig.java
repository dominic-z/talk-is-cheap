package org.talk.is.cheap.orm.mybatis.hello.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper;


/**
 * 用于创建Mapper对象，并根据mapper对象的注解和xm文件内容进行匹配。
 */
@Configuration
@MapperScan(basePackageClasses = BlogMapper.class)
public class MybatisConfig {
}
