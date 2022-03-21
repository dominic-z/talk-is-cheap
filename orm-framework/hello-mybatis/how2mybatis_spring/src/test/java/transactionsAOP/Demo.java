package transactionsAOP;

import domain.Author;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import service.transactionAOP.AuthorService;

import java.util.List;

public class Demo {
    Author getAuthor() {
        Author author = new Author();
        author.setName("dom");
        author.setAge(20.1f);
        author.setSexType(0);
        author.setSex("MALE");
        return author;
    }

    @Test
    void testXML() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("transactionsAOP/spring-config.xml");

        AuthorService authorSevice = ac.getBean("authorService", AuthorService.class);

        authorSevice.insertAuthor(getAuthor());

        List<Author> authorList = authorSevice.selectAuthors();
        System.out.println(authorList);
    }

}
