package org.talk.is.cheap.project.free.flow.starter.repository.dao.customized;

import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.ClusterNodeLog;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* DAO for customized sql defined in  customized/mapper.xml
* for example
* public List<ClusterNodeLog> findByIds(Collection<Long> ids) {
*       if (ids.isEmpty()) {
*       return new ArrayList<>();
*       }
*       Map<String, Object> params = new HashMap<>();
*       params.put("ids", ids);
*       return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
*  }
*
* @author dominiczhu
* @date 2025/09/22
*/
@Repository
public class ClusterNodeLogDao {

    private static final String TABLE = "org.talk.is.cheap.project.free.flow.starter.repository.dao.customized.ClusterNodeLogDao";

    @Autowired
    @Qualifier("repositoryStarterSqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

}
