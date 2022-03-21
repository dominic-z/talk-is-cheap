package service;

import domain.Author;
import domain.Blog;

public interface BlogService {
    Blog getBlog();

    Blog getBlog(Author author);
}
