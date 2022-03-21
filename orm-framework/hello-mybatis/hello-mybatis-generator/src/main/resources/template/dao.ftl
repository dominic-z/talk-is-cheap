package ${customizedDaoPackage};

import ${modelPackage}.${domainUpperCamelName};
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
* public List<${domainUpperCamelName}> findByIds(Collection<Long> ids) {
*       if (ids.isEmpty()) {
*       return new ArrayList<>();
*       }
*       Map<String, Object> params = new HashMap<>();
*       params.put("ids", ids);
*       return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
*  }
*
* @author ${author}
* @date ${date}
*/
@Component
public class ${daoUpperCamelName} {

    private static final String TABLE = "${customizedDaoPackage}.${daoUpperCamelName}";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

}
