package org.talk.is.cheap.project.free.flow.scheduler.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.project.free.flow.scheduler.repository.dao.mbg.SchedulerLogMapper;

@Configuration
// 必须指定sqlSessionTemplateRef，因为算上repositoryStarter的sqlSessionTemplate，容器里实际上会有两个sqlSessionTemplate，应该使用当前项目中的，而不是starter里的
@MapperScan(basePackageClasses = {SchedulerLogMapper.class}, sqlSessionTemplateRef = "sqlSessionTemplate")
public class MybatisConfig {
}
