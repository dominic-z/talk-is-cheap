package dao;

import domain.Author;
import domain.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BlogMapper {
    Blog selectById(int id);
    List<Blog> selectAllBlogs();

    void insertABlog(Blog blog);
    void insertABlogReturnKey(Blog blog);
    void insertBlogList(List<Blog> blogs);
    void insertBlogArr(Blog[] blogs);

    Blog selectByIdWithConstructor(int id);
    Blog selectByIdWithOnlyConstructor(int id);
    Blog selectByIdWithAssociation(int id);
    Author selectAuthorById(int id);
    Blog selectByIdWithNestedSelectAssociation(int id);
    Blog selectByIdWithNestedResultsAssociation(int id);//功能同selectByIdWithAssociation
    List<Blog> selectAllBlogsWithNestedSelectAssociation();
    List<Blog> selectAllBlogsWithNestedResultsAssociation();

    Blog selectToShowRiskOfAutoMapping(int id);

    List<Blog> selectBlogsByIf(@Param("content") String content, @Param("author") Author author);
    List<Blog> selectBlogsByChoose(@Param("content") String content, @Param("author") Author author);

    List<Blog> selectBlogsByWhere(@Param("content") String content, @Param("author") Author author);
    List<Blog> selectBlogsByTrimWhere(@Param("content") String content, @Param("author") Author author);
    void updateBlogBySet(@Param("id") int id,@Param("content") String content,@Param("authorId")Integer authorId,@Param("authorName") String authorName);
    void updateBlogByTrimSet(@Param("id") int id,@Param("content") String content,@Param("authorId")Integer authorId,@Param("authorName") String authorName);

    List<Blog> selectBlogsByIdList(@Param("ids") List<Integer> ids);
    List<Blog> selectBlogsByAuthorMapId(@Param("authorIdAuthorMap") Map<Integer,Author> authorIdAuthorMap);
    List<Blog> selectBlogsByAuthorMapName(@Param("authorIdAuthorMap") Map<Integer,Author> authorIdAuthorMap);

}
