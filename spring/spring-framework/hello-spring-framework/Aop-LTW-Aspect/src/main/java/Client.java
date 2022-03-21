import config.AppConfig;
import domain.Author;
import domain.Blog;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import service.BlogService;
import service.BlogServiceImp;

public class Client {
    public static void main(String[] args) {
//       jvm argument -javaagent:spring-instrument-5.2.5.RELEASE.jar
//        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
//      XML context doesn't seem to do this job
        ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
        Author author = ac.getBean("author", Author.class);
        BlogService blogService = ac.getBean("blogServiceImp", BlogService.class);
        blogService.getBlog(author);
        new BlogServiceImp().getBlog();

        System.out.println(new Blog());
    }
}
