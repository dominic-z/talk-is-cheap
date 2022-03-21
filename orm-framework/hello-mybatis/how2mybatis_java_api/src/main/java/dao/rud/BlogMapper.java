package dao.rud;

import domain.Blog;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface BlogMapper {
    @Select("select * from blog")
    @ResultType(Blog.class)
    List<Blog> selectBlogs();

    @Insert("insert into blog (content,authorId,authorName)values(#{content},#{authorId},#{authorName});")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertBlog(Blog blog);

    @Delete("delete from how2mybatis.blog where id>=5;")
    void deleteBlogs();

    @Update("ALTER TABLE how2mybatis.blog auto_increment=5;")
    void updateBlogTable();
}
