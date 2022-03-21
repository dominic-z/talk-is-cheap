package chapter1_7;

import domain.Author;
import myException.ServiceLayerException;
import myException.ServiceLayerRuntimeException;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import service.chapter1_5.AuthorPlatformTxManagerService;
import service.chapter1_5.AuthorTxTemplateService;
import service.chapter1_7.TxBoundEventService;

public class Demo {
    @Test
    public void testTxEvent(){
        ApplicationContext ac=new ClassPathXmlApplicationContext("chapter1_7/TxBoundEventContext.xml");
        TxBoundEventService authorServiceImp=ac.getBean("authorService", TxBoundEventService.class);
//        authorServiceImp.insertAuthorException(new Author());
        authorServiceImp.insertAuthorRuntimeException(new Author());
    }


}
