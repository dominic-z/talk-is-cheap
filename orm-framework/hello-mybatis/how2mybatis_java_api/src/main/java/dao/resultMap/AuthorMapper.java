package dao.resultMap;

import domain.Author;
import domain.Sex;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;
import java.util.List;

public interface AuthorMapper {
    @Select("select * from author where id=#{id}")
    Author selectById(int id);


    @Select("select a.id         as aid,\n" +
            "               a.name       as name,\n" +
            "               a.age        as age,\n" +
            "               a.sexType    as sexType,\n" +
            "               a.sex        as sex\n" +
            "        from author a where a.id = #{id};")
    @Results(id = "authorMapCollection", value = {
            @Result(id = true, property = "id", column = "aid"),
            @Result(property = "name", column = "name"),
            @Result(property = "age", column = "age"),
            @Result(property = "sexType", column = "sexType"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "sexEnum", column = "sex"),
            @Result(property = "blogList", column = "aid", javaType = ArrayList.class,
                    many = @Many(select = "dao.resultMap.BlogMapper.selectBlogsByAuthorId"))
    })
    Author selectByIdWithCollection(int id);



    @Select("select a.id         as aid,\n" +
            "               a.name       as name,\n" +
            "               a.age        as age,\n" +
            "               a.sexType    as sexType,\n" +
            "               a.sex        as sex\n" +
            "        from author a;")
    @Results(id = "authorsMapNestedSelectCollection", value = {
            @Result(id = true, property = "id", column = "aid"),
            @Result(property = "name", column = "name"),
            @Result(property = "age", column = "age"),
            @Result(property = "sexType", column = "sexType"),
            @Result(property = "sex", column = "sex"),
            @Result(property = "sexEnum", column = "sex"),
            @Result(property = "blogList", column = "aid", javaType = ArrayList.class,
                    many = @Many(select = "dao.resultMap.BlogMapper.selectBlogsByWiredAuthorId"))
    })
    List<Author> selectAuthorsByIdWithCollection();
}
