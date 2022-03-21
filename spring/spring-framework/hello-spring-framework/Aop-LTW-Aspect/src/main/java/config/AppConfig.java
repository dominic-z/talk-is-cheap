package config;

import domain.Author;
import domain.Blog;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import service.BlogService;
import service.BlogServiceImp;

@EnableLoadTimeWeaving
@Configuration
public class AppConfig {
    @Bean
    public Author author(){
        Author author =new Author();
        author.setId(999);
        author.setName("Andy");
        return author;
    }

    @Bean("blog")
    public Blog blog(Author author){
        Blog blog=new Blog();
        blog.setId(4);
        blog.setAuthor(author);
        return blog;
    }

    @Bean("blogServiceImp")
    public BlogService blogService(){
        return new BlogServiceImp();
    }
}
