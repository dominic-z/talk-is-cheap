package org.talk.is.cheap.orm.mybatis.hello.dao;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.talk.is.cheap.orm.mybatis.hello.config.SqlSessionConfig;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Blog;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlogDao {


    private static final String namespace = "org.talk.is.cheap.orm.mybatis.hello.dao.BlogDao";
    private static final SqlSession sqlSession;

    static {
        try {
            sqlSession = SqlSessionConfig.getSqlSession();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Blog> selectByIds(List<Integer> ids) {

        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        return sqlSession.selectList(namespace + ".selectByIds", params);
    }


    /**
     * 伪分页
     * @param ids
     * @param offset
     * @param limit
     * @return
     */
    public List<Blog> selectByIds(List<Integer> ids, int offset, int limit) {

        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        return sqlSession.selectList(namespace + ".selectByIds", params, new RowBounds(offset, limit));
    }


}
