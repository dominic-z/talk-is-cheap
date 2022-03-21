package jvm.understandjvm.chapter8.section4;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MethodDispatcherDemo
 * @date 2021/10/22 上午11:36
 */
public class MethodDispatcherDemo1 {

    class GrandFather {
        void thinking() {
            System.out.println("i am grandfather");
        }
    }
    class Father extends GrandFather {
        void thinking() {
            System.out.println("i am father");
        }
    }
    class Son extends Father {
        void thinking() {
// 请读者在这里填入适当的代码（不能修改其他地方的代码）
// 实现调用祖父类的thinking()方法，打印"i am grandfather"

            try {

//                final Method thinking = GrandFather.class.getDeclaredMethod("thinking");
//                thinking.setAccessible(true);
//                thinking.invoke(this);


                MethodType mt = MethodType.methodType(void.class);
                final MethodHandle thinking1 =
                        MethodHandles.lookup().findSpecial(GrandFather.class, "thinking", mt,getClass()).bindTo(this);
                thinking1.invoke();


                final Field lookupImpl = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                lookupImpl.setAccessible(true);
                MethodHandle mh = ((MethodHandles.Lookup) lookupImpl.get(null)).findSpecial(GrandFather.class,
                        "thinking",mt,getClass());
                mh.invoke(this);

            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        MethodDispatcherDemo1 demo=new MethodDispatcherDemo1();

        final Son son = demo.new Son();
        son.thinking();
    }
}
