package dao.sqlSessionFactoryBean;

import domain.Author;
import org.apache.ibatis.annotations.Param;

public interface AuthorMapper {

    Author selectById(@Param("userId") int userId);
}
