package org.example.backend.config;

import com.alibaba.cloud.nacos.annotation.NacosConfigListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 监听配置信息变更事件
 */
@Configuration
@Slf4j
public class NacosConfigEventListener {

    @Autowired
    ConfigFromNacos configFromNacos;

//    修改nacosconfig1这个配置文件的时候，会将完整的config作为入参通知回来
    @NacosConfigListener(dataId = "nacosconfig1",group = "DEV_GROUP")
    public void updateConfig1(String config) {
        log.info("new config1:"+config);
        log.info(configFromNacos.getInfo1());
    }

    @NacosConfigListener(dataId = "nacosconfig2",group = "DEV_GROUP",initNotify = true)
    public void updateConfig2(String config) {
        log.info("new config2:"+config);
        log.info(configFromNacos.getInfo2());
    }
}
