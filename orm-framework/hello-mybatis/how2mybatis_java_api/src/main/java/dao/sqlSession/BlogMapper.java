package dao.sqlSession;

import domain.Blog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BlogMapper {
    @Select("select * from blog where id=#{id}")
    Blog selectById(int id);

    @Select("select * from blog")
    List<Blog> selectBlogs();
}
