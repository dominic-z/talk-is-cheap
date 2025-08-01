package org.talk.is.cheap.orm.mybatis.hello.mappers;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Blog;

import java.util.List;

public interface BlogMapper {


    @Select("select * from blog")
    @ResultType(Blog.class)
    List<Blog> selectBlogs();

    List<Blog> selectByIds(@Param("ids") List<Integer> ids);

}
