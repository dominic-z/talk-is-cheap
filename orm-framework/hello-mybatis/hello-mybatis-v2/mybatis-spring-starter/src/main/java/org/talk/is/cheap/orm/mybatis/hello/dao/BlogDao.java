package org.talk.is.cheap.orm.mybatis.hello.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Blog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BlogDao {


    private static final String namespace = "org.talk.is.cheap.orm.mybatis.hello.dao.BlogDao";

    @Autowired
    private  SqlSessionTemplate sqlSessionTemplate;


    public List<Blog> selectByIds(List<Integer> ids) {

        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        return sqlSessionTemplate.selectList(namespace + ".selectByIds", params);
    }

}
