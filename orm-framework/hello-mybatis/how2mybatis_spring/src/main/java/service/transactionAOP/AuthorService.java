package service.transactionAOP;

import dao.transactionsAOP.AuthorMapper;
import domain.Author;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("authorService")
public class AuthorService {

    @Resource(name="authorMapper")
    AuthorMapper authorMapper;

    public List<Author> selectAuthors(){
        return authorMapper.selectAuthors();
    }

    public void insertAuthor(Author author){
        authorMapper.insertAuthor(author);
        int i=1/0;
    }
}
