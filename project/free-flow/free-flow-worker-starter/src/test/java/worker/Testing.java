package worker;


import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Testing {


    static class Parent<T>{

    }

    static class Child<T> extends Parent<T>{
        boolean classEqual(Class<?> clazz){
            Type genericSuperclass = this.getClass().getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                // 获取父类的泛型参数（此处为User.class）
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length > 0) {
                    Type type = actualTypeArguments[0];
                    System.out.println("父类的泛型参数类型：" + type.getTypeName()+"??"+type.getClass()); // 输出：User

                }
            }
            return true;
        }
    }

    @Test
    public void test(){
//        System.out.println(CaseFormat.LOWER_HYPHEN.converterTo(CaseFormat.LOWER_CAMEL).convert("connect-url"));
//        new CuratorConfig().zkConfigProperties();

//        System.out.println(Paths.get("/aa","fdsfds"));

        System.out.println(new Child<String>().classEqual(String.class));

    }
}
