package service.chapter1_7;

import dao.AuthorMapper;
import domain.Author;
import myException.ServiceLayerException;
import myException.ServiceLayerRuntimeException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.transaction.annotation.Transactional;
@Transactional
public class TxBoundEventServiceImp implements TxBoundEventService, ApplicationEventPublisherAware {
    public void setAuthorMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    private AuthorMapper authorMapper;

    @Override
    @Transactional(rollbackFor = ServiceLayerException.class)
    public void insertAuthorException(Author author) throws ServiceLayerException {
        author.setName("exception");
        authorMapper.insertAuthor(author);
        if (true) {
            throw new ServiceLayerException("业务层编译期错误");//默认情况下不会触发回滚
        }
        author.setName("exception2");
        authorMapper.insertAuthor(author);
    }

    @Override
    @Transactional
    public void insertAuthorRuntimeException(Author author) {
        author.setName("runtime");
        authorMapper.insertAuthor(author);
        if (true) {
            this.publisher.publishEvent(new RollbackEvent(this,"fail"));
            throw new ServiceLayerRuntimeException("业务层运行期异常");
        }
        author.setName("runtime2");
        authorMapper.insertAuthor(author);
    }

    private ApplicationEventPublisher publisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public ApplicationEventPublisher getPublisher() {
        return publisher;
    }
}
