package service.chapter1_4;

import domain.Author;
import myException.ServiceLayerException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface AuthorServiceAnno {
    Author getAuthorById(int id);

    void insertAuthorException(Author author) throws ServiceLayerException;
    void insertAuthorRuntime(Author author);

    void updateAuthorById(int id, Author author) throws ServiceLayerException;
}
