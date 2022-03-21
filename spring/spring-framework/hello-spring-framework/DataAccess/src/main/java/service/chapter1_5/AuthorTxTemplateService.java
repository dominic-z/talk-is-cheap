package service.chapter1_5;

import domain.Author;
import myException.ServiceLayerException;

public interface AuthorTxTemplateService {

    void insertAuthorException(Author author);
    void insertAuthorRuntimeException(Author author);

}
