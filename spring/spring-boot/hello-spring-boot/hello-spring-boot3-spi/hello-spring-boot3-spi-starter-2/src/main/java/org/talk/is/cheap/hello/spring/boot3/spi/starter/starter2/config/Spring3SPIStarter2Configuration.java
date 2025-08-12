package org.talk.is.cheap.hello.spring.boot3.spi.starter.starter2.config;


import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.hello.spring.boot3.spi.common.pojo.Product;

@Configuration
@AutoConfigureAfter(name = "hello.spring.boot3.spi.starter.starter.config.Spring3SPIStarterConfiguration")
//@AutoConfigureBefore(name = "hello.spring.boot3.spi.starter.starter.config.Spring3SPIStarterConfiguration")
public class Spring3SPIStarter2Configuration {


    @Bean(name = "another-product")
    public Product product(){
        System.out.println("starter2中的product创建");
        Product product = new Product();
        product.setName("another-product");
        return product;
    }

}
