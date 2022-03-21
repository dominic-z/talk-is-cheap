package domain.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import domain.enums.Gender;
import lombok.Data;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Teacher
 * @date 2021/9/3 上午11:23
 */
@Data
public class Teacher {

    private String name;

    private Gender gender;

    @JsonCreator
    public static Teacher parse(@JsonProperty("nameP") String nameP, @JsonProperty("genderP") Gender genderP) {
        System.out.println("parse(@JsonProperty)" + nameP + " " + genderP);
        Teacher teacher = new Teacher();
        teacher.setName(nameP);
        teacher.setGender(genderP);
        return teacher;
    }
}
