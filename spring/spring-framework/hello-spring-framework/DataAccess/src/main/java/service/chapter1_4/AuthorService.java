package service.chapter1_4;

import domain.Author;
import myException.ServiceLayerException;

public interface AuthorService {
    Author getAuthorById(int id);

    void insertAuthorException(Author author) throws ServiceLayerException;
    void insertAuthorRuntimeException(Author author);

    void updateAuthorById(int id, Author author) throws ServiceLayerException;
}
