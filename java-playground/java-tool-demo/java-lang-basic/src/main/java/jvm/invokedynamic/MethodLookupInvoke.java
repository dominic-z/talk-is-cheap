package jvm.invokedynamic;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * @author dominiczhu
 * @version 1.0
 * @title InvokeExtact
 * @date 2021/8/18 上午11:17
 */
public class MethodLookupInvoke {
    public static void bar(Object o) {
        new Exception().printStackTrace();
    }

    private Integer getInt(String s, float f, Long l) {
        System.out.println(l);
        System.out.println(s);
        return (int) f;
    }

    public static void main(String[] args) throws Throwable {
        MethodHandles.Lookup l = MethodHandles.lookup();
        MethodType barType = MethodType.methodType(void.class, Object.class);
        MethodHandle bar = l.findStatic(MethodLookupInvoke.class, "bar", barType);
        System.out.println("=========invoke static=========");
        bar.invokeExact(new Object());
        bar.invoke("");


        System.out.println("=========invoke method=========");
        MethodType getIntType = MethodType.methodType(Integer.class, String.class, Float.TYPE, Long.class);
        MethodHandle getInt = l.findVirtual(MethodLookupInvoke.class, "getInt", getIntType);
        System.out.println("getInt type: "+getInt.type());
        Integer intValue = (Integer) getInt.invokeExact(new MethodLookupInvoke(), "ssss", 12f, new Long(20));
        System.out.println("getInt res: " + intValue);

        MethodHandle bindToGetInt = getInt.bindTo(new MethodLookupInvoke());
        Integer bindToValue = (Integer) bindToGetInt.invokeExact("ssss", 12f, new Long(20));
        System.out.println("type: "+bindToValue);
        System.out.println("bindType: "+bindToGetInt.type());


        System.out.println("=========show form=========");
        Field lambdaFormField = MethodHandle.class.getDeclaredField("form");
        lambdaFormField.setAccessible(true);

        Object barForm = lambdaFormField.get(bar);
        System.out.println("barForm: " + barForm.toString());

        Object getIntForm = lambdaFormField.get(getInt);
        System.out.println("getIntForm: " + getIntForm.toString());

    }
}
