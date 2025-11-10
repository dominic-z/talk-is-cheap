package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;

import java.util.List;

@Service
@Slf4j
public class TaskExecutionServiceWrapper {

    @Autowired
    private TaskExecutionService taskExecutionService;

    public TaskExecution selectById(long id) {
        TaskExecutionExample example = new TaskExecutionExample();
        example.createCriteria().andIdEqualTo(id);
        List<TaskExecution> list = taskExecutionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}
