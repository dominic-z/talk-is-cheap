package org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.customized;

import org.talk.is.cheap.project.free.flow.scheduler.cluster.domain.pojo.ClusterNodeRegistryLog;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* DAO for customized sql defined in  customized/mapper.xml
* for example
* public List<ClusterNodeRegistryLog> findByIds(Collection<Long> ids) {
*       if (ids.isEmpty()) {
*       return new ArrayList<>();
*       }
*       Map<String, Object> params = new HashMap<>();
*       params.put("ids", ids);
*       return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
*  }
*
* @author dominiczhu
* @date 2025/08/02
*/
@Component
public class ClusterNodeRegistryLogDao {

    private static final String TABLE = "org.talk.is.cheap.project.free.flow.scheduler.cluster.dao.customized.ClusterNodeRegistryLogDao";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

}
