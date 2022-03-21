package dao;

import domain.Author;
import org.apache.ibatis.annotations.Param;

public interface AuthorMapper {
    Author getAuthorById(int id);

    void insertAuthor(Author author);

    void updateAuthorById(@Param("id") int id,@Param("author") Author author);
}
