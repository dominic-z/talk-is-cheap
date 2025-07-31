package org.talk.is.cheap.orm.mybatis.hello.domain.pojo;


import lombok.Data;
import org.talk.is.cheap.orm.mybatis.hello.domain.enums.Sex;

import java.util.List;

@Data
public class Author {
    private Integer id;
    private String name;
    private Integer age;
    private Double height;
    private Integer sexValue;
    private String sex;
    private Sex sexEnum;
    private List<Blog> blogList;
}
