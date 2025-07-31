package org.talk.is.cheap.orm.mybatis.hello.mappers;

import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Author;

import java.util.ArrayList;
import java.util.List;

public interface AuthorMapper {
    @Select("select * from author where id=#{id};")
    Author selectById(int id);

    String SELECT_BY_ID_WITH_COLLECTION_SQL = """
            select a.id         as aid,
                   a.name       as name,
                   a.age        as age,
                   a.sex_value    as sex_value,
                   a.sex        as sex
            from author a where a.id = #{id};
            """;
    @Select(SELECT_BY_ID_WITH_COLLECTION_SQL)
    @Results(id = "authorMapCollection", value = {
            @Result(id = true, property = "id", column = "aid"),
            @Result(property = "name", column = "name"),
            @Result(property = "age", column = "age"),
            @Result(property = "sexType", column = "sexType"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "sexEnum", column = "sex"),
            @Result(property = "blogList", column = "aid", javaType = ArrayList.class,
                    many = @Many(select = "org.talk.is.cheap.orm.mybatis.hello.mappers.BlogMapper.selectBlogsByAuthorId"))
    })
    Author selectByIdWithCollection(int id);



}
