package service.chapter1_4;

import dao.AuthorMapper;
import domain.Author;
import myException.ServiceLayerException;
import myException.ServiceLayerRuntimeException;

public class AuthorServiceImp implements AuthorService {

    public void setAuthorMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    private AuthorMapper authorMapper;

    @Override
    public Author getAuthorById(int id) {
        Author author = authorMapper.getAuthorById(id);
        return author;
    }

    @Override
    public void insertAuthorException(Author author) throws ServiceLayerException {
        author.setName(author.getName() + "_exception");
        authorMapper.insertAuthor(author);
        if (true) {
//            spring事务默认情况下只会对RuntimeException进行回滚，其余异常不会触发回滚
            throw new ServiceLayerException("业务层编译期错误");//默认情况下不会触发回滚
        }
        author.setName(author.getName() + "_exception2");
        authorMapper.insertAuthor(author);

    }

    @Override
    public void insertAuthorRuntimeException(Author author) {
        author.setName(author.getName() + "_runtime");
        authorMapper.insertAuthor(author);
        if (true) {
//            spring事务默认情况下只会对RuntimeException进行回滚，其余异常不会触发回滚
            throw new ServiceLayerRuntimeException("业务层运行期异常");//默认情况下会触发回滚
        }
        author.setName(author.getName() + "_runtime2");
        authorMapper.insertAuthor(author);
    }

    @Override
    public void updateAuthorById(int id, Author author) {
        authorMapper.updateAuthorById(id, author);
    }
}
