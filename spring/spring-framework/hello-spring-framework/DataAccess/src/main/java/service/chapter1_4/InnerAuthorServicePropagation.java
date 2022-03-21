package service.chapter1_4;

import domain.Author;

public interface InnerAuthorServicePropagation {
    void insertRequire(Author author,boolean ifThrow);

    void insertRequireNew(Author author,boolean ifThrow);
    void insertRequireNested(Author author,boolean ifThrow);
}
