package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class TaskStartupServiceWrapper {

    @Autowired
    private TaskStartupService taskStartupService;

    public TaskStartup selectById(long id, int... statuses) {
        TaskStartupExample example = new TaskStartupExample();
        TaskStartupExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        if (statuses != null && statuses.length != 0) {
            criteria.andStatusIn(Arrays.stream(statuses).boxed().toList());
        }
        List<TaskStartup> list = taskStartupService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    public int updateByIdSelective(long id, TaskStartup taskStartup, Long revision) {
        TaskStartupExample example = new TaskStartupExample();
        TaskStartupExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        if (revision != null) {
            criteria.andRevisionEqualTo(revision);
            taskStartup.setRevision(revision + 1);
        }
        return taskStartupService.updateByExampleSelective(taskStartup, example);
    }
}
