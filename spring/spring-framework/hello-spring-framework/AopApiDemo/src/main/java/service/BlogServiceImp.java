package service;

import domain.Author;
import domain.Blog;

public class BlogServiceImp implements BlogService {

    @Override
    public Blog getBlog() {
        System.out.println("生成blog中");
        Blog blog = new Blog();
        blog.setId(121);
        return blog;
    }

    @Override
    public Blog getBlog(Author author) {
        System.out.println("生成blog中");
        Blog blog = new Blog();
        blog.setId(123);
        blog.setAuthor(author);
        return blog;
    }
}
