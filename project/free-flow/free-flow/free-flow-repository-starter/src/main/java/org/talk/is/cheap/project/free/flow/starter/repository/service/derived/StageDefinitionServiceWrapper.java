package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageDefinitionExample;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.StageStartupExample;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageDefinition;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageStartup;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageDefinitionService;
import org.talk.is.cheap.project.free.flow.starter.repository.service.StageStartupService;

import java.util.List;

@Service
@Slf4j
public class StageDefinitionServiceWrapper {
    @Autowired
    private StageDefinitionService stageDefinitionService;

    public StageDefinition selectById(long id) {
        StageDefinitionExample example = new StageDefinitionExample();
        example.createCriteria().andIdEqualTo(id);
        List<StageDefinition> list = stageDefinitionService.selectByExample(example);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}
