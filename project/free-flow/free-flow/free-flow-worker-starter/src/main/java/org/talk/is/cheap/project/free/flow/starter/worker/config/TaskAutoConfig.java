package org.talk.is.cheap.project.free.flow.starter.worker.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.controller.TaskDefinitionController;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.service.LocalTaskDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.controller.TaskDriverController;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.runtime.TaskRuntimeService;
import org.talk.is.cheap.project.free.flow.starter.worker.task.driver.service.TaskDriverService;

/**
 * task相关的自动配置，包括
 * 1. service
 * 2. controller
 */
@Configuration
@ComponentScan(basePackageClasses = {LocalTaskDefinitionService.class, TaskDriverService.class, TaskDefinitionController.class,
        TaskRuntimeService.class, TaskDriverController.class})
public class TaskAutoConfig {
}
