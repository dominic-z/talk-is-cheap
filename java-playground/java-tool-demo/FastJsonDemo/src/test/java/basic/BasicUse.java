package basic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import domain.Student;
import domain.enums.Gender;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title BasicUse
 * @date 2021/2/23 下午5:28
 */
public class BasicUse {

    public Student getStudent() {
        Student stu = new Student();
        stu.setAge(10);
//        stu.setName("dom");
        stu.setGender(Gender.MALE);
        stu.setBirthDay(new Date());

        return stu;
    }


    @Test
    public void toBytes(){

        Map<String,byte[]> map = new HashMap<>();

        map.put("abc","你好".getBytes());
        String jsonString = JSON.toJSONString(map);
        System.out.println(jsonString);

    }

    @Test
    public void toStringMap() {
        String jsonString = "{\"name\":\"jacky\",\"time\":1563957295755,\"age\":23}";
        Map<String, String> map = JSON.parseObject(jsonString, new TypeReference<Map<String, String>>() {
        });

        System.out.println(map);
        System.out.println(map.get("name"));

        JSONObject json = new JSONObject();
        json.put("abc", "");
        System.out.println(json.toJSONString());
        System.out.println(json.getString("dcf"));
    }


    @Test
    public void toJsonStringAndParseArr() {
        List<Student> students = new ArrayList<>();
        students.add(getStudent());
        students.add(getStudent());
        String jsonString = JSON.toJSONString(students);
        System.out.println(jsonString);


        List<Student> stuArr = JSON.parseArray(jsonString, Student.class);
        System.out.println(stuArr);
        List<Student> stuList = JSON.parseObject(jsonString, new TypeReference<List<Student>>() {
        });
        System.out.println(stuList);

    }

    @Test
    public void getListObj() {
        List<Student> students = new ArrayList<>();
        students.add(getStudent());
        students.add(getStudent());

        JSONObject inputObj = new JSONObject();
        inputObj.put("students", students);
        List<Student> stuList = inputObj.getObject("students", new TypeReference<List<Student>>() {
        });
        System.out.println(stuList);
    }

    @Test
    public void getEnumObj() {
        JSONObject enumJson = new JSONObject();
        enumJson.put("gender", Gender.FEMALE);

        System.out.println(enumJson.getObject("gender", Gender.class));

    }

    @Test
    public void obj2JsonObj() {
        JSONObject stuJson = (JSONObject) JSON.toJSON(new Student());
        stuJson.put("other", "other");
        Student student = JSON.parseObject(stuJson.toJSONString(), Student.class);
        System.out.println(student);
    }

    @Test
    public void EscapeChapter() {
        String withoutEscapeStr = "{\"student\":{\"name\":\"jacky\",\"time\":1563957295755,\"age\":23}}";
        JSONObject withoutEscapeJSON = JSON.parseObject(withoutEscapeStr);

        System.out.println(withoutEscapeJSON.toJSONString());
        System.out.println(withoutEscapeJSON.getObject("student", Student.class));
        System.out.println("raw string " + withoutEscapeJSON.getString("student"));

        Student student = JSON.parseObject(withoutEscapeJSON.getString("student"), Student.class);
        System.out.println("student " + student);


        System.out.println("=========================================");

        String withEscapeStr = "{\"students\":\"[{\\\"name\\\":\\\"jacky\\\",\\\"time\\\":1563957295755," +
                "\\\"age\\\":23}]\"}";
        JSONObject withEscapeJSON = JSON.parseObject(withEscapeStr);

        System.out.println(withEscapeJSON.toJSONString());
//        System.out.println(withEscapeJSON.getObject("student",Student.class));
        System.out.println("raw string " + withEscapeJSON.getString("students"));
        System.out.println("toJson " + JSON.toJSON(withEscapeJSON.getString("students")));
        List<Student> students = JSON.parseObject(withEscapeJSON.getString("students"),
                new TypeReference<List<Student>>() {
                });
        System.out.println("students " + students);

        JSONArray studentJSONArray = JSON.parseArray(withEscapeJSON.getString("students"));
        System.out.println("studentJSONArray " + studentJSONArray);

    }

}
