package dao.resultMap;

import domain.Author;
import domain.Blog;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import typeHandlers.AuthorNameHandler;
import wiredDomain.AuthorName;

import java.util.List;

public interface BlogMapper {
    @Select("select id as _id, content as _content, authorId as _authorId, authorName as _authorName\n" +
            "        from blog\n" +
            "        where id = #{id};")
    @Results(id = "blogMap", value = {
            @Result(id = true, property = "id", column = "_id"),
            @Result(property = "content", column = "_content"),
            @Result(property = "authorId", column = "_authorId"),
            @Result(property = "authorName", column = "_authorName"),
            @Result(property = "authorNameObj", column = "_authorName",
                    javaType = AuthorName.class, jdbcType = JdbcType.VARCHAR,
                    typeHandler = AuthorNameHandler.class)
    })
    Blog selectById(int id);


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
            @Result(property = "authorObj", javaType = Author.class, column = "authorId",
                    one = @One(select = "dao.resultMap.AuthorMapper.selectById"))
    })
    Blog selectByIdWithAssociation(int id);

    @Select("select * from blog where authorId=#{authorId}")
    List<Blog> selectBlogsByAuthorId(int authorId);

    @Select("select * from blog where authorId <= #{authorId}")
    List<Blog> selectBlogsByWiredAuthorId(int authorId);
}
