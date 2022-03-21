package service.chapter1_5;

import dao.AuthorMapper;
import domain.Author;
import myException.ServiceLayerException;
import myException.ServiceLayerRuntimeException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class AuthorTxTemplateImp implements AuthorTxTemplateService {

    private AuthorMapper authorMapper;
    private final TransactionTemplate transactionTemplate;

    public AuthorTxTemplateImp(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);

        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    public void setAuthorMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }


    @Override
    public void insertAuthorException(Author author) {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            try {
                author.setName(author.getName() + "_exception");
                authorMapper.insertAuthor(author);
                if (true)
                    throw new ServiceLayerException("业务层编译期错误");
                author.setName(author.getName() + "_exception2");
                authorMapper.insertAuthor(author);
            } catch (ServiceLayerException e) {
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
            }
        });


    }

    @Override
    public void insertAuthorRuntimeException(Author author) {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            try {
                author.setName(author.getName() + "_exception");
                authorMapper.insertAuthor(author);
                if (true)
                    throw new ServiceLayerRuntimeException("业务层运行期错误");
                author.setName(author.getName() + "_exception2");
                authorMapper.insertAuthor(author);
            } catch (ServiceLayerRuntimeException e) {
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
            }
        });
    }

}
