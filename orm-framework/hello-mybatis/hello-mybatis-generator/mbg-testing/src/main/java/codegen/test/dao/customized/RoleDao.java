package codegen.test.dao.customized;

import codegen.test.domain.pojo.Role;
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
* public List<Role> findByIds(Collection<Long> ids) {
*       if (ids.isEmpty()) {
*       return new ArrayList<>();
*       }
*       Map<String, Object> params = new HashMap<>();
*       params.put("ids", ids);
*       return sqlSessionTemplate.selectList(TABLE + ".select_by_ids", params);
*  }
*
* @author dominiczhu
* @date 2024/01/18
*/
@Component
public class RoleDao {

    private static final String TABLE = "codegen.test.dao.customized.RoleDao";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;


    public List<Role> selectByIds(List<Integer> ids){
        Map<String,Object> params = new HashMap<>();
        params.put("ids",ids);
        return sqlSessionTemplate.selectList(TABLE+".select_by_ids",params);
    }

}
