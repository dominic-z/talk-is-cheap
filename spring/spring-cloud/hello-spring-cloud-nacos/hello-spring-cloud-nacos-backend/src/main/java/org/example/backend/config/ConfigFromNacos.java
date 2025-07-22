package org.example.backend.config;

import com.alibaba.cloud.nacos.annotation.NacosConfig;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * 读取配置中心
 */
@Configuration
@Data
// 反复尝试是出来的，按照nacos官网来说，NacosConfig自带更新对应的配置项，但是我发现不加这个注解就不会自动更新，搞不懂
@RefreshScope
public class ConfigFromNacos {

//    可以使用@Value，也可以使用@NacosConfig，当使用@Value的时候，不需要写前缀，比如配置里面的yaml是config1:info1: xxx，那么value里面只需要写${info1}就行了
//    也是反复试出来的
    @Value("${info1}")
//    @NacosConfig(dataId = "nacosconfig1",group = "DEV_GROUP",key = "config1.info1")
    private String info1;


    @NacosConfig(dataId = "nacosconfig2",group = "DEV_GROUP",key = "config2.info")
    private String info2;


}
