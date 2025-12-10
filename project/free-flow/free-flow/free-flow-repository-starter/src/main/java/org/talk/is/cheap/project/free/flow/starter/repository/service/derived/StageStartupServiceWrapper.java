package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class StageStartupServiceWrapper {
    @Autowired
    private StageStartupService stageStartupService;

    public StageStartup selectById(long id,int... statuses) {
        StageStartupExample stageStartupExample = new StageStartupExample();
        StageStartupExample.Criteria criteria = stageStartupExample.createCriteria();
        criteria.andIdEqualTo(id);
        if(statuses!=null && statuses.length!=0){
            criteria.andStatusIn(Arrays.stream(statuses).boxed().toList());
        }
        List<StageStartup> stageStartups = stageStartupService.selectByExample(stageStartupExample);
        if (stageStartups.isEmpty()) {
            return null;
        }
        return stageStartups.get(0);
    }


    public int updateSelectiveById(long id, StageStartup stageStartup) {
        StageStartupExample stageStartupExample = new StageStartupExample();
        stageStartupExample.createCriteria().andIdEqualTo(id);
        return stageStartupService.updateByExampleSelective(stageStartup, stageStartupExample);
    }

}
