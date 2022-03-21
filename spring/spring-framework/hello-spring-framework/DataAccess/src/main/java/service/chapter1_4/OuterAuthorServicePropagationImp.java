package service.chapter1_4;

import dao.AuthorMapper;
import domain.Author;
import myException.ServiceLayerRuntimeException;
import org.springframework.transaction.annotation.Transactional;

public class OuterAuthorServicePropagationImp implements OuterAuthorServicePropagation {
    private InnerAuthorServicePropagation innerAuthorServicePropagation;

    public void setInnerAuthorServicePropagation(InnerAuthorServicePropagation innerAuthorServicePropagation) {
        this.innerAuthorServicePropagation = innerAuthorServicePropagation;
    }

    public void setAuthorMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    private AuthorMapper authorMapper;

    @Override
    @Transactional
    public void insertRequire(Author author,boolean innerThrow,boolean outerThrow) {
        author.setName("outer_require");
        authorMapper.insertAuthor(author);
        try {
            innerAuthorServicePropagation.insertRequire(author, innerThrow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(outerThrow)
            throw new ServiceLayerRuntimeException("外部运行期异常");
    }

    @Override
    @Transactional
    public void insertRequireNew(Author author,boolean innerThrow,boolean outerThrow) {
        author.setName("outer_requires_new");
        authorMapper.insertAuthor(author);
        try {
            innerAuthorServicePropagation.insertRequireNew(author, innerThrow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(outerThrow)
            throw new ServiceLayerRuntimeException("外部运行期异常");
    }

    @Override
    @Transactional
    public void insertNested(Author author, boolean innerThrow, boolean outerThrow) {
        author.setName("outer_nested");
        authorMapper.insertAuthor(author);
        try {
            innerAuthorServicePropagation.insertRequireNested(author, innerThrow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(outerThrow)
            throw new ServiceLayerRuntimeException("外部运行期异常");
    }
}
