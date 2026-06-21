package org.talk.is.cheap.java.tool.demo.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class StudentSource {
    private Date createTime;
    private String firstName;
    private String lastName;
}
