package service.chapter1_7;

import domain.Author;
import myException.ServiceLayerException;

public interface TxBoundEventService {
    void insertAuthorException(Author author) throws ServiceLayerException;
    void insertAuthorRuntimeException(Author author);
}
