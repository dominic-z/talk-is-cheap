package domain;

import org.apache.ibatis.annotations.Param;
import wiredDomain.AuthorName;

import java.util.List;

public class Blog {
    private int id;
    private String content;
    private int authorId;
    private String authorName;
    private int coAuthorId;

    private Author authorObj;
    private AuthorName authorNameObj;

    public Blog(){}
    public Blog(@Param("id") int id,@Param("content") String content){
        System.out.println("带参构造函数调用");
        this.id=id;
        this.content=content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public int getCoAuthorId() {
        return coAuthorId;
    }

    public void setCoAuthorId(int coAuthorId) {
        this.coAuthorId = coAuthorId;
    }

    public Author getAuthorObj() {
        return authorObj;
    }

    public void setAuthorObj(Author authorObj) {
        this.authorObj = authorObj;
    }

    public AuthorName getAuthorNameObj() {
        return authorNameObj;
    }

    public void setAuthorNameObj(AuthorName authorNameObj) {
        this.authorNameObj = authorNameObj;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                '}';
    }
}
