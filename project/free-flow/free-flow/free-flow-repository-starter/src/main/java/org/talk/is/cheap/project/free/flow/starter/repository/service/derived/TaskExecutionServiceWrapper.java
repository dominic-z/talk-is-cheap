package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TaskExecutionServiceWrapper {

    @Autowired
    private TaskExecutionService taskExecutionService;

    public TaskExecution selectById(long id, int... taskStatus) {
        TaskExecutionExample example = new TaskExecutionExample();
        TaskExecutionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        if (taskStatus != null && taskStatus.length != 0) {
            criteria.andStatusIn(Arrays.stream(taskStatus).boxed().toList());
        }
        List<TaskExecution> list = taskExecutionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    public int updateSelectiveById(long id, TaskExecution taskExecution, Long revision) {
        TaskExecutionExample example = new TaskExecutionExample();
        TaskExecutionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        if (revision != null) {
            criteria.andRevisionEqualTo(revision);
            taskExecution.setRevision(revision + 1);
        }
        return taskExecutionService.updateByExampleSelective(taskExecution, example);
    }

}
