package reflect;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Reflection {

    interface InterfazeParent<J>{

    }

    class Parent<T>{
        T t;
        public T encode(){
            return null;
        }

        public String decode(T t){
            return "aaa";
        }
    }

    class Child extends Parent<String> implements InterfazeParent<Integer>{


    }

    class Child2<T> extends Parent<T>{

    }

    class GrandChild extends Child{

    }

    @Test
    public void readGenericClass(){
        Class<?> childClass = Child.class;
        System.out.println(Arrays.toString(childClass.getGenericInterfaces()));
        logGenericSupperClass(childClass);
        logGenericSupperClass(GrandChild.class);
        logGenericSupperClass(Object.class);
        logGenericSupperClass(Child2.class);

        logGenericInterface(childClass);
        logGenericInterface(GrandChild.class);
        logGenericInterface(Object.class);
    }

    private static void logGenericInterface(Class<?> clazz){
        System.out.println("***********************************"+clazz);

        System.out.println(Arrays.toString(clazz.getGenericInterfaces()));
    }

    private static void logGenericSupperClass(Class<?> childClass) {
        System.out.println("==============================================="+childClass);
        Type genericSuperclass = childClass.getGenericSuperclass();
        System.out.println(genericSuperclass);
        if(genericSuperclass instanceof ParameterizedType parameterizedType){
            System.out.println("是泛型父类"+genericSuperclass);
            // 获取泛型参数类型（这里是User.class）
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            // 输出结果
            for (Type type : actualTypeArguments) {

                // https://www.doubao.com/thread/w4134c21c12f6cb9f
                // 判断是否为具体类型（Class 实例）
                if (type instanceof Class) {
                    System.out.println("  类型：具体类型（" + ((Class<?>) type).getSimpleName() + "）");
                }
                // 判断是否为单纯的泛型变量（TypeVariable 实例）
                else if (type instanceof TypeVariable) {
                    TypeVariable<?> typeVariable = (TypeVariable<?>) type;
                    System.out.println("  类型：单纯的泛型变量（名称：" + typeVariable.getName() + "）");
                }
                // 其他情况（如嵌套泛型、通配符等）
                else {
                    System.out.println("  类型：复杂类型（" + type.getClass().getSimpleName() + "）");
                }

            }
        }else{

            if(genericSuperclass!=null){
                System.out.println("具备泛型父类，但是需要递归深入查"+genericSuperclass);
                logGenericSupperClass(childClass.getSuperclass());
            }else {
                System.out.println("不是泛型父类");
            }
        }
    }

    /*
    判断一个类是fou是另一个类的父类
     */
    @Test
    public void testSuper(){
        System.out.println(Parent.class.isAssignableFrom(Child.class));;
        System.out.println(Child.class.isAssignableFrom(Child.class));;
        System.out.println(Child.class.isAssignableFrom(Parent.class));;
        System.out.println(InterfazeParent.class.isAssignableFrom(Child.class));;
    }

}
