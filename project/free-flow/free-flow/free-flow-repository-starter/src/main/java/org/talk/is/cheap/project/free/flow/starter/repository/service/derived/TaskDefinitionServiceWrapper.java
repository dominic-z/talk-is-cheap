package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
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

    public List<TaskDefinition> selectByIds(List<Long> ids) {
        TaskDefinitionExample example = new TaskDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<TaskDefinition> list = taskDefinitionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list;
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


    public int updateSelectiveById(long id, TaskDefinition taskDefinition, Long revision) {
        TaskDefinitionExample example = new TaskDefinitionExample();
        TaskDefinitionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        if (revision != null) {
            criteria.andRevisionEqualTo(revision);
            taskDefinition.setRevision(revision + 1);
        }
        return taskDefinitionService.updateByExampleSelective(taskDefinition, example);
    }


    public int updateSelectiveByNameVersion(String name,Integer version, TaskDefinition taskDefinition, Long revision) {
        TaskDefinitionExample example = new TaskDefinitionExample();
        TaskDefinitionExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name).andVersionEqualTo(version);
        if (revision != null) {
            criteria.andRevisionEqualTo(revision);
            taskDefinition.setRevision(revision + 1);
        }
        return taskDefinitionService.updateByExampleSelective(taskDefinition, example);
    }

}
