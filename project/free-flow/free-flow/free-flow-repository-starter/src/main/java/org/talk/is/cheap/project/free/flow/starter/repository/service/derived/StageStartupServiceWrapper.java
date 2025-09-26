package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;

import java.util.List;

@Service
@Slf4j
public class StageStartupServiceWrapper {

    private StageStartupService stageStartupService;

    public StageStartup selectById(long id) {
        StageStartupExample stageStartupExample = new StageStartupExample();
        stageStartupExample.createCriteria().andIdEqualTo(id);
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
