package jvm.invokedynamic;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Circuit
 * @date 2021/8/18 下午4:49
 */


class Horse {
    public void race() {
        System.out.println("Horse.race()");
    }
}

class Deer {
    public void race() {
        System.out.println("Deer.race()");
    }
}

// javac Circuit.java
// java Circuit
public class Circuit {

    public static void startRace(Object obj) {
        // aload obj
        // invokedynamic race()
    }

    public static void main(String[] args) {
        startRace(new Horse());
        // startRace(new Deer());
    }

    public static CallSite bootstrap(MethodHandles.Lookup l, String name, MethodType callSiteType) throws Throwable {
        MethodHandle mh = l.findVirtual(Horse.class, name, MethodType.methodType(void.class));
        return new ConstantCallSite(mh.asType(callSiteType));
    }
}