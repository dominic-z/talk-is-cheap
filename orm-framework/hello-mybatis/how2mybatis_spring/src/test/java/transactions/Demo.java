package transactions;

import dao.transactions.AuthorMapper;
import domain.Author;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

public class Demo {
    Author getAuthor(){
        Author author=new Author();
        author.setName("dom");
        author.setAge(20.1f);
        author.setSexType(0);
        author.setSex("MALE");
        return author;
    }

    @Test
    void testXML() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("transactions/spring-config.xml");

        DataSourceTransactionManager transactionManager = ac.getBean("transactionManager",
                DataSourceTransactionManager.class);
//      相当于对事务进行配置，配置隔离级别等
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 事物隔离级别，开启新事务，这样会比较安全些。
        TransactionStatus status = transactionManager.getTransaction(def); // 获得事务状态

        AuthorMapper authorMapper = ac.getBean("authorMapper", AuthorMapper.class);
        List<Author> authorList = authorMapper.selectAuthors();
        System.out.println(authorList);

        try{
            authorMapper.insertAuthor(getAuthor());
            int i=1/0;
            transactionManager.commit(status);
        }catch (Exception e){
            transactionManager.rollback(status);
            throw e;
        }
    }

}
