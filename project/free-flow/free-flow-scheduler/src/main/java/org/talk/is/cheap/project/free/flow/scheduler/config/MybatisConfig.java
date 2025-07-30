package org.talk.is.cheap.project.free.flow.scheduler.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.mbg.ClusterNodeRegistryLogMapper;

@Configuration
@MapperScan(basePackageClasses = {ClusterNodeRegistryLogMapper.class})
public class MybatisConfig {
}
