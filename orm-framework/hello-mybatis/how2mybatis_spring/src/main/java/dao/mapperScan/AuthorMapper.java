package dao.mapperScan;

import domain.Author;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AuthorMapper {
    @Select("SELECT * FROM author WHERE id = #{userId}")
    Author selectById(@Param("userId") int userId);
}
