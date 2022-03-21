package domain.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import domain.enums.Gender;
import lombok.Data;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Student
 * @date 2021/9/3 上午11:02
 */
@Data
public class Student {

    @JsonValue
    private String name;

    private Gender gender;

    @JsonCreator
    public static Student parse(String serialized) {
        System.out.println("parse String: " + serialized);
        Student student = new Student();
        student.setName(serialized);
        return student;
    }
}
