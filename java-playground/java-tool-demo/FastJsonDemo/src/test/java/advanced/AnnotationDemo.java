package advanced;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import domain.Teacher;
import org.junit.Test;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AnnotationDemo
 * @date 2021/6/25 下午5:03
 */
public class AnnotationDemo {


    @Test
    public void testJSONField(){
        Teacher teacher = new Teacher();
        String jsonString = JSON.toJSONString(teacher);

        System.out.println(jsonString);

        Teacher jsonObject = JSON.parseObject(jsonString,Teacher.class);
        System.out.println(jsonObject);

    }
}
