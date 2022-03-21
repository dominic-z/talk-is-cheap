package service.chapter1_4;

import dao.AuthorMapper;
import domain.Author;
import myException.ServiceLayerRuntimeException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class InnerAuthorServicePropagationImp implements InnerAuthorServicePropagation {
    public void setAuthorMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    private AuthorMapper authorMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void insertRequire(Author author,boolean ifThrow) {
        author.setName("inner_require");
        authorMapper.insertAuthor(author);
        if(ifThrow)
            throw new ServiceLayerRuntimeException("内部运行期异常");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void insertRequireNew(Author author,boolean ifThrow) {
        author.setName("inner_require_new");
        authorMapper.insertAuthor(author);
        if(ifThrow)
            throw new ServiceLayerRuntimeException("内部运行期异常");
    }

    @Transactional(propagation = Propagation.NESTED)
    @Override
    public void insertRequireNested(Author author,boolean ifThrow) {
        author.setName("inner_nested");
        authorMapper.insertAuthor(author);
        if(ifThrow)
            throw new ServiceLayerRuntimeException("内部运行期异常");
    }
}
