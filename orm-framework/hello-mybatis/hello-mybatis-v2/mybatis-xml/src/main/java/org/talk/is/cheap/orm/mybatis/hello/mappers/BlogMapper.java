package org.talk.is.cheap.orm.mybatis.hello.mappers;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Author;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.AuthorName;
import org.talk.is.cheap.orm.mybatis.hello.domain.pojo.Blog;
import org.talk.is.cheap.orm.mybatis.hello.typeHandlers.AuthorNameHandler;

import java.util.List;
import java.util.Map;

public interface BlogMapper {


    @Select("select * from blog")
    @ResultType(Blog.class)
    List<Blog> selectBlogs();

    List<Blog> selectByIds(@Param("ids") List<Integer> ids);


    @Insert("insert into blog (content,author_id,author_name)values(#{content},#{authorId},#{authorName});")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Blog blog);

    @Delete("delete from blog where id=#{id};")
    int deleteById(Integer id);

    @Update("ALTER TABLE blog auto_increment=5;")
    int updateBlogAutoIncrement();


    // 将结果直接做成一个map
    @Select("select * from blog where author_id=#{authorId}")
    @MapKey("id")
    //which is a property used as the key of the map.
    Map<Integer, Blog> selectBlogsByAuthorId(int authorId);



    String SELECT_BY_ID_SQL= """
            select
               id as _id,
               content as _content,
               author_id as _authorId,
               author_name as _authorName
            from blog
            where id = #{id};
            """;
    // 测试resultMap
    @Select(SELECT_BY_ID_SQL)
    @Results(id = "blogMap", value = {
            @Result(id = true, property = "id", column = "_id"),
            @Result(property = "content", column = "_content"),
            @Result(property = "authorId", column = "_authorId"),
            @Result(property = "authorName", column = "_authorName"),
//            将_authorName转换为对应的类
            @Result(property = "authorNameObj", column = "_authorName",
                    javaType = AuthorName.class, jdbcType = JdbcType.VARCHAR,
                    typeHandler = AuthorNameHandler.class)
    })
    Blog selectById(int id);



    // 测试使用指定的构造函数
    @Select("select * from blog where id=#{id}")
    @Results(id = "blogMapConstructor", value = {
            @Result(property = "authorId", column = "_authorId"),
            @Result(property = "authorName", column = "_authorName"),
            @Result(property = "authorNameObj", column = "_authorName",
                    javaType = AuthorName.class, jdbcType = JdbcType.VARCHAR,
                    typeHandler = AuthorNameHandler.class)
    })
    @ConstructorArgs(value = {
            @Arg(id = true, name = "id", column = "id", javaType = int.class),
            @Arg(name = "content", column = "content", javaType = String.class)
    })
    Blog selectByIdWithConstructor(int id);



    @Select("select * from blog where id=#{id}")
    @Results(id = "blogMapAssociation", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "content", column = "content"),
            @Result(property = "author", javaType = Author.class, column = "author_id", // 异常排查，第一次column的列名写错了，写了一个结果不存在的列名，导致这个one都没有触发。
                    one = @One(select = "org.talk.is.cheap.orm.mybatis.hello.mappers.AuthorMapper.selectById"))
    })
    Blog selectByIdWithAssociation(int id);

}
