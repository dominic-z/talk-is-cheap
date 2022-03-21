package dao;

import domain.Author;
import domain.Blog;
import domain.Sex;
import domain.SexDetail;
import org.apache.ibatis.annotations.Param;
import wiredDomain.AuthorName;

import java.util.List;

public interface AuthorMapper {
    Author selectById(int id);
    Author selectByName(String name);
    AuthorName selectAuthorNameById(int id);
    Author selectByAuthorNameObj(@Param("authorNameObj") AuthorName authorName);

    Sex selectAuthorSexById(int id);
    SexDetail selectAuthorSexByIdWithIndex(int id);

    Author selectByIdWithSimpleSql(int id);
    Author selectByIdWithNestedSql(int id);

    Author selectByAuthorBean(Author author);
    Author selectByArgs(@Param("aId") int aId,@Param("aName") String aName);
    List<Author> selectAllAuthorsOrderBy(@Param("columnName") String columnName);
    Author selectByTableAndId(@Param("tabelName") String tabelName,@Param("aId")int aId);
    Author selectByAuthorName(@Param("authorName") AuthorName authorName);
    Author selectByAuthorNameAndId(AuthorName authorName,int id);

    Author selectByIdWithCollection(int id);
    List<Blog> selectBlogsByAuthorId(int id);
    Author selectByIdWithNestedSelectColleciton(int id);
    List<Blog> selectBlogsByWiredId(int id);
    List<Author> selectAuthorsByIdWithNestedSelectColleciton();

    Author selectByIdWithDiscriminator(int id);
    Author selectByIdWithDiscriminatorExtended(int id);

}
