package service.chapter1_5;

import dao.AuthorMapper;
import domain.Author;
import myException.ServiceLayerException;
import myException.ServiceLayerRuntimeException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class AuthorPlatformTxManagerServiceImp implements AuthorPlatformTxManagerService {

    private AuthorMapper authorMapper;
    private final PlatformTransactionManager transactionManager;

    public AuthorPlatformTxManagerServiceImp(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;

    }

    public void setAuthorMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }


    @Override
    public void insertAuthorException(Author author) throws ServiceLayerException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = this.transactionManager.getTransaction(def);
        try {
            author.setName(author.getName() + "_exception");
            authorMapper.insertAuthor(author);
            if (true)
                throw new ServiceLayerException("业务层编译期错误");
            author.setName(author.getName() + "_exception2");
            authorMapper.insertAuthor(author);
        } catch (ServiceLayerException e) {
            transactionManager.rollback(status);
            throw e;
        }
        transactionManager.commit(status);

    }

    @Override
    public void insertAuthorRuntimeException(Author author) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = this.transactionManager.getTransaction(def);
        try {
            author.setName(author.getName() + "_exception");
            authorMapper.insertAuthor(author);
            if (true)
                throw new ServiceLayerRuntimeException("业务层运行期错误");
            author.setName(author.getName() + "_exception2");
            authorMapper.insertAuthor(author);
        } catch (ServiceLayerRuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
        transactionManager.commit(status);
    }

}
