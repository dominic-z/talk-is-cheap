package domain;

import java.util.List;

public class Author {
    private int id;
    private String name;
    private float age;
    private int sexType;
    private String sex;
    private Sex sexEnum;

    private List<Blog> blogList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAge() {
        return age;
    }

    public void setAge(float age) {
        this.age = age;
    }

    public int getSexType() {
        return sexType;
    }

    public void setSexType(int sexType) {
        this.sexType = sexType;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Sex getSexEnum() {
        return sexEnum;
    }

    public void setSexEnum(Sex sexEnum) {
        this.sexEnum = sexEnum;
    }

    public List<Blog> getBlogList() {
        return blogList;
    }

    public void setBlogList(List<Blog> blogList) {
        this.blogList = blogList;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sexType=" + sexType +
                ", sex='" + sex + '\'' +
                '}';
    }
}
