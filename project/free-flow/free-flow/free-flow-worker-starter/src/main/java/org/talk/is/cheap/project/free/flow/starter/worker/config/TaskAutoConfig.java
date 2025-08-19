package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service.TaskDriverService;

@Configuration
@ComponentScan(basePackageClasses = {LocalTaskDefinitionService.class, TaskDriverService.class})
public class TaskAutoConfig {
}
