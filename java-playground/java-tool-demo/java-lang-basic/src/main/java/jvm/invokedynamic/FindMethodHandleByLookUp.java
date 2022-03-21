package jvm.invokedynamic;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LookUp
 * @date 2021/8/18 上午10:57
 */
public class FindMethodHandleByLookUp {
    private static void bar(Object o) {
    }

    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    public static void main(String[] args) throws Exception {
        MethodHandles.Lookup lookup = FindMethodHandleByLookUp.lookup();

        Method m = FindMethodHandleByLookUp.class.getDeclaredMethod("bar", Object.class);
        MethodHandle mh0 = lookup.unreflect(m);

        MethodType t = MethodType.methodType(void.class, Object.class);
        MethodHandle mh1 = lookup.findStatic(FindMethodHandleByLookUp.class, "bar", t);
    }


}