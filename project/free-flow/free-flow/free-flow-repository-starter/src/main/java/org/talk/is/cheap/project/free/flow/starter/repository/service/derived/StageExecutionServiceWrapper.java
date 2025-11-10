package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;

import java.util.List;

@Service
@Slf4j
public class StageExecutionServiceWrapper {
    @Autowired
    private StageExecutionService stageExecutionService;

    public StageExecution selectById(long id) {
        StageExecutionExample example = new StageExecutionExample();
        example.createCriteria().andIdEqualTo(id);
        List<StageExecution> list = stageExecutionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}
