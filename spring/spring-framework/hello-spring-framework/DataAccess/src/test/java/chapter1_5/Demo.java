package chapter1_5;

import domain.Author;
import myException.ServiceLayerException;
import myException.ServiceLayerRuntimeException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import service.chapter1_5.AuthorPlatformTxManagerService;
import service.chapter1_5.AuthorTxTemplateService;

public class Demo {
    @Test
    public void testTransactionTemplate(){
        ApplicationContext ac=new ClassPathXmlApplicationContext("chapter1_5/TxTemplateContext.xml");
        AuthorTxTemplateService authorServiceImp=ac.getBean("authorService", AuthorTxTemplateService.class);
        authorServiceImp.insertAuthorException(new Author());
        authorServiceImp.insertAuthorRuntimeException(new Author());
    }


    @Test
    public void testPlatformTxManager(){
        ApplicationContext ac=new ClassPathXmlApplicationContext("chapter1_5/PlatformTxManagerContext.xml");
        AuthorPlatformTxManagerService authorServiceImp=ac.getBean("authorService", AuthorPlatformTxManagerService.class);
        try {
            authorServiceImp.insertAuthorException(new Author());
        } catch (ServiceLayerException e) {
            e.printStackTrace();
        }
        try {
            authorServiceImp.insertAuthorRuntimeException(new Author());
        } catch (ServiceLayerRuntimeException e) {
            e.printStackTrace();
        }
    }
}
