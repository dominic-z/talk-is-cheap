package dao.mapperScan;

import domain.Blog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface BlogMapper {
    @Select("SELECT * FROM blog WHERE id = #{userId}")
    Blog selectById(@Param("userId") int id);
}
