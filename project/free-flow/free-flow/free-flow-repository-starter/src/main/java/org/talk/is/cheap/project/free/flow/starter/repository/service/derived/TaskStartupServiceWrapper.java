package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskStartupService;

import java.util.List;

@Service
@Slf4j
public class TaskStartupServiceWrapper {

    @Autowired
    private TaskStartupService taskStartupService;

    public TaskStartup selectById(long id) {
        TaskStartupExample example = new TaskStartupExample();
        example.createCriteria().andIdEqualTo(id);
        List<TaskStartup> list = taskStartupService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}
