package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskDefinitionService;

import java.util.List;

@Service
@Slf4j
public class TaskDefinitionServiceWrapper {

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

}
