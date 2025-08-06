package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.TaskDefinitionValidator;

@Configuration
@ComponentScan(basePackageClasses = {TaskDefinitionValidator.class})
public class TaskAutoConfig {
}
