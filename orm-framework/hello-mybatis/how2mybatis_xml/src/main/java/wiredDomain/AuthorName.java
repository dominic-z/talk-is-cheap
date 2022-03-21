package wiredDomain;

public class AuthorName {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "我是：AuthorName{" +
                "name='" + name + '\'' +
                '}';
    }
}
