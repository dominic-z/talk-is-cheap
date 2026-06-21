package org.talk.is.cheap.orm.mybatis.hello.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talk.is.cheap.orm.mybatis.hello.dao.BlogDao;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Blog;
import org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper;

import java.util.List;

@Service
@Slf4j
public class BlogService {


    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private BlogDao blogDao;

    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;


    public List<Blog> selectByIdsMapper(List<Integer> ids) {
        return blogMapper.selectByIds(ids);
    }

    public List<Blog> selectByIdsDao(List<Integer> ids) {
        return blogDao.selectByIds(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createBlog() {
        Blog blog = Blog.builder()
                .authorId(1)
                .authorName("haha")
                .content("content").build();
        blogMapper.insert(blog);
        int i = 1 / 0;
        log.info("blog : {}", blog);
    }
}
