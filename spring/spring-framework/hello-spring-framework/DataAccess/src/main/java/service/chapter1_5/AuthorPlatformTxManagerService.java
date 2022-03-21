package service.chapter1_5;

import domain.Author;
import myException.ServiceLayerException;

public interface AuthorPlatformTxManagerService {

    void insertAuthorException(Author author) throws ServiceLayerException;
    void insertAuthorRuntimeException(Author author);

}
