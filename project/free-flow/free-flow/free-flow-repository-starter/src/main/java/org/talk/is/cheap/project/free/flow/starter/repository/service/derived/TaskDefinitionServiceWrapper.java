package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;

import java.util.List;

@Service
@Slf4j
public class TaskDefinitionServiceWrapper {
    @Autowired
    private TaskDefinitionService taskDefinitionService;

    public TaskDefinition selectById(long id) {
        TaskDefinitionExample example = new TaskDefinitionExample();
        example.createCriteria().andIdEqualTo(id);
        List<TaskDefinition> list = taskDefinitionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     *
     * @param name
     * @param version 如果为null，则尝试获取最新的taskDefinition
     * @return
     */

    public TaskDefinition queryByNameVersion(String name, Integer version) {
        TaskDefinitionExample example = new TaskDefinitionExample();

        if (version != null) {
            example.createCriteria()
                    .andNameEqualTo(name)
                    .andVersionEqualTo(version);
        } else {
            example.createCriteria()
                    .andNameEqualTo(name);
            example.setOrderByClause(String.format("%s desc", TaskDefinition.VERSION));
        }
        List<TaskDefinition> taskDefinitions = taskDefinitionService.selectByExample(example);
        if (taskDefinitions.isEmpty()) {
            return null;
        }
        return taskDefinitions.get(0);
    }

}
