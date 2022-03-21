package chapter1_4;

import domain.Author;
import myException.ServiceLayerException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import service.chapter1_4.AuthorService;
import service.chapter1_4.OuterAuthorServicePropagation;

public class Demo {
    Author getAuthor() {
        Author author = new Author();
        author.setName("dominic");
        return author;
    }

    @Test
    public void testXML() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("chapter1_4/XMLTxContext.xml");
        AuthorService authorService = ac.getBean("authorService", AuthorService.class);
        Author author = getAuthor();
        try {
            authorService.insertAuthorException(author);
        } catch (ServiceLayerException e) {
            e.printStackTrace();
        }
        authorService.insertAuthorRuntimeException(author);
    }

    @Test
    public void testAnno() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("chapter1_4/AnnoTxContext.xml");
        AuthorService authorService = ac.getBean("authorServiceAnno", AuthorService.class);
        Author author = getAuthor();
        try {
            authorService.insertAuthorException(author);
        } catch (ServiceLayerException e) {
            e.printStackTrace();
        }
        authorService.insertAuthorRuntimeException(author);
    }

    @Test
    public void testPropagationRequire() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("chapter1_4/PropagationContext.xml");
        OuterAuthorServicePropagation authorService = ac.getBean("outerAuthorService", OuterAuthorServicePropagation.class);

        try {
//            内部抛异常，外部不抛异常，结果为全部回滚
            authorService.insertRequire(new Author(), true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
//            内部不抛异常，外部抛异常，结果为全部回滚
            authorService.insertRequire(new Author(), false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPropagationRequiresNew() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("chapter1_4/PropagationContext.xml");
        OuterAuthorServicePropagation authorService = ac.getBean("outerAuthorService", OuterAuthorServicePropagation.class);
        try {
//            内部抛异常，外部不抛异常，结果为内部回滚，外部提交
            authorService.insertRequireNew(new Author(), true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
//            内部不抛异常，外部抛异常，结果为内部提交，外部回滚
            authorService.insertRequireNew(new Author(), false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPropagationNested() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("chapter1_4/PropagationContext.xml");
        OuterAuthorServicePropagation authorService = ac.getBean("outerAuthorService", OuterAuthorServicePropagation.class);
        try {
//            内部抛异常，外部不抛异常，结果为内部回滚，外部提交
            authorService.insertNested(new Author(), true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
//            内部不抛异常，外部抛异常，结果为内部回滚，外部回滚
            authorService.insertNested(new Author(), false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
