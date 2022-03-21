package dao.transactions;

import domain.Author;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AuthorMapper {
    @Select("select * from author where id=#{userId}")
    Author selectById(@Param("userId") int userId);

    @Select("select * from author")
    @ResultType(Author.class)
    List<Author> selectAuthors();

    @Insert("insert into author (name,age,sexType,sex) values(#{name},#{age},#{sexType},#{sex})")
    void insertAuthor(Author author);

    @Delete("delete from author where id>=4")
    void deleteAuthors();

    @Update("alter table author auto_increment=4")
    void updateAuthorTable();

}
