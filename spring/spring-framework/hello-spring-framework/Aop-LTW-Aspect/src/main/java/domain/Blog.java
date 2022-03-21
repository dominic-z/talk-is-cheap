package domain;

public class Blog {
    private int id;

    private Author author;

    public void init() {
        System.out.println("blog init");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Blog{" +
                "id=" + id +
                ", author=" + author +
                '}';
    }


}
