package service;

import domain.Author;

public class AuthorService  {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author getAuthor() {
        System.out.println("生成author中");
        Author author=new Author();
        author.setId(99);
        author.setName("Ivy");
        return author;
    }
}
