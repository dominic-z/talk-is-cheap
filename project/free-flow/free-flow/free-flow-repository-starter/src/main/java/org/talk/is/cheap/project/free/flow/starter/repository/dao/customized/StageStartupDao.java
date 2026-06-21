package org.talk.is.cheap.project.free.flow.starter.repository.dao.customized;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.StageCountGroupByTaskStatus;

import java.util.List;
import java.util.Map;

/**
* DAO for customized sql defined in  customized/mapper.xml
* for example
* public List<StageStartup> findByIds(Collection<Long> ids) {
*       if (ids.isEmpty()) {
*       return new ArrayList<>();
*       }
*       Map<String, Object> params = new HashMap<>();
*       params.put("ids", ids);
*       return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
*  }
*
* @author dominiczhu
* @date 2025/12/16
*/
@Repository
public class StageStartupDao {

    private static final String TABLE = "org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.StageStartupDao";

    @Autowired
    @Qualifier("repositoryStarterSqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    public List<StageCountGroupByTaskStatus> countGroupByTaskStatus(List<Long> taskExecutionIds){
        return sqlSessionTemplate.selectList(TABLE+".count_group_by_task_status",Map.of("task_execution_ids",taskExecutionIds));
    }

}
