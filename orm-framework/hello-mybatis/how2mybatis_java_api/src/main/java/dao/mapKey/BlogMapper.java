package dao.mapKey;

import domain.Blog;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

public interface BlogMapper {
    @Select("select * from blog where authorId=#{authorId}")
    @MapKey("id")//which is a property used as the key of the map.
    Map<Integer,Blog> selectBlogsByAuthorId(int authorId);
}
