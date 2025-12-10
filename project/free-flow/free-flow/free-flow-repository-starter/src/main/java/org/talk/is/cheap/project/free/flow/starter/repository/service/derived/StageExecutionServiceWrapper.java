package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.common.enums.TaskStageStatus;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.TaskExecutionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.TaskExecution;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageExecutionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.TaskExecutionService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StageExecutionServiceWrapper {
    @Autowired
    private StageExecutionService stageExecutionService;

    public StageExecution selectById(long id, int... statuses) {
        StageExecutionExample example = new StageExecutionExample();
        StageExecutionExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        if(statuses!=null && statuses.length!=0){
            criteria.andStatusIn(Arrays.stream(statuses).boxed().toList());
        }
        List<StageExecution> list = stageExecutionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    public List<StageExecution> selectByStartupId(long stageStartupId, int... taskStageStatuses) {
        StageExecutionExample example = new StageExecutionExample();
        StageExecutionExample.Criteria criteria = example.createCriteria();
        criteria.andStageStartupIdEqualTo(stageStartupId);
        if(taskStageStatuses!=null && taskStageStatuses.length!=0){
            criteria.andStatusIn(Arrays.stream(taskStageStatuses).boxed().toList());
        }
        return stageExecutionService.selectByExample(example);
    }


    public int updateSelectiveById(long id, StageExecution stageExecution){
        StageExecutionExample example = new StageExecutionExample();
        example.createCriteria().andIdEqualTo(id);

        return stageExecutionService.updateByExampleSelective(stageExecution,example);
    }
}
