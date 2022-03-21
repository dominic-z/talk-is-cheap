package domain;

import com.alibaba.fastjson.annotation.JSONField;
import domain.enums.Gender;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Student
 * @date 2021/2/23 下午5:29
 */
@Data
@ToString
public class Student {
   private String name;
   private int age;
   private Gender gender;

//   @JSONField(format = "yyyyMMdd")
   private Date birthDay;

}
