package service.chapter1_4;

import domain.Author;

public interface OuterAuthorServicePropagation {
    void insertRequire(Author author,boolean innerThrow,boolean outerThrow);
    void insertRequireNew(Author author,boolean innerThrow,boolean outerThrow);
    void insertNested(Author author, boolean innerThrow, boolean outerThrow);
}
