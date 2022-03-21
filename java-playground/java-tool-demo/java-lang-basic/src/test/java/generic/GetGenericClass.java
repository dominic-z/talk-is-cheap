package generic;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author dominiczhu
 * @version 1.0
 * @title GetGenericClass
 * @date 2021/10/14 下午4:49
 */
public class GetGenericClass {

    @Test
    public void getGenericClass(){

        // 获取父类上的泛型
        final Class1 class1 = new Class1();
        final ParameterizedType genericSuperclass = (ParameterizedType)class1.getClass().getGenericSuperclass();

        for (Type generic:genericSuperclass.getActualTypeArguments()){
            System.out.println(generic);
        }


        final Type[] genericInterfaces = class1.getClass().getGenericInterfaces();

        for (Type interfacc:genericInterfaces){
            for (Type generic:((ParameterizedType)interfacc).getActualTypeArguments()){
                System.out.println(generic);
            }
        }
        
    }


}
