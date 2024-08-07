package org.talk.is.cheap.project.what.to.eat.dao.customized;

import org.talk.is.cheap.project.what.to.eat.domain.pojo.BusinessMarketingPlan;
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
* public List<BusinessMarketingPlan> findByIds(Collection<Long> ids) {
*       if (ids.isEmpty()) {
*       return new ArrayList<>();
*       }
*       Map<String, Object> params = new HashMap<>();
*       params.put("ids", ids);
*       return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
*  }
*
* @author dominiczhu
* @date 2023/08/07
*/
@Component
public class BusinessMarketingPlanDao {

    private static final String TABLE = "org.talk.is.cheap.project.what.to.eat.dao.customized.BusinessMarketingPlanDao";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

}
