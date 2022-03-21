package others;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.enums.Gender;
import domain.pojo.Student;
import domain.pojo.Teacher;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AnnotationDemo
 * @date 2021/9/3 上午11:01
 */
public class AnnotationDemo {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testJsonValue() throws JsonProcessingException {

        // 序列化的时候，如果出现了JsonValue注解，那么会根据该注解来进行序列化
        // 并且如果是枚举类，反序列化也会使用被注解的字段，并且一个类里只能有一个JsonValue注解
        Gender male = Gender.MALE;
        Map<String, Gender> genderMap = new HashMap<>();
        genderMap.put("male", male);

        String genderMapJson = objectMapper.writeValueAsString(genderMap);
        System.out.println(genderMapJson);
        Map<String, Gender> parsedGenderMap = objectMapper.readValue(genderMapJson, new TypeReference<Map<String,
                Gender>>() {
        });
        System.out.println(parsedGenderMap);


    }


    @Test
    public void testJsonCreator() throws JsonProcessingException {

        // jsonCreator是控制反序列化功能，可以直接传入一个string，然后自己进行反序列化
        Map<String, Student> parsedStuMap = objectMapper.readValue("{\"stu\":\"marry\"}",
                new TypeReference<Map<String,
                        Student>>() {
                });
        System.out.println(parsedStuMap);


        Student stu = objectMapper.readValue("\"abc,asfdas\"", Student.class);
        System.out.println(stu);
    }

    @Test
    public void testJsonCreatorProperty() throws JsonProcessingException {
        // jsonCreator 配合JsonProperty可以实现自定义字段与参数名的映射规则

        Map<String, String> teacherMap = new HashMap<>();
        teacherMap.put("nameP", "name");
        teacherMap.put("genderP", "male");
        String teacherMapJson = objectMapper.writeValueAsString(teacherMap);
        System.out.println(teacherMapJson);
        Teacher teacher = objectMapper.readValue(teacherMapJson, Teacher.class);
        System.out.println(teacher);
    }

}
