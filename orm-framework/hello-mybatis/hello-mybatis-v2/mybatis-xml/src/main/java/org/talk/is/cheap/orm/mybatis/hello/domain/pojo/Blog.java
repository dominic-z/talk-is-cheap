package org.talk.is.cheap.orm.mybatis.hello.domain.pojo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Param;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Blog {

    private Integer id;
    private String content;
    private Integer authorId;
    private String authorName;

    private AuthorName authorNameObj;
    private Author author;


    public Blog(@Param("id") int id, @Param("content") String content){
        System.out.println("带参构造函数调用");
        this.id=id;
        this.content=content;
    }
}
