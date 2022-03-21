
// 需要更改ASMHelper.MyMethodVisitor中的BOOTSTRAP_CLASS_NAME
import java.lang.invoke.*;

public class MonomorphicInlineCache {

    private final MethodHandles.Lookup lookup;
    private final String name;

    public MonomorphicInlineCache(MethodHandles.Lookup lookup, String name) {
        this.lookup = lookup;
        this.name = name;
    }

    private Class<?> cachedClass = null;
    private MethodHandle mh = null;

    public void invoke(Object receiver) throws Throwable {
        System.out.println("invoke");
        System.out.println(cachedClass);
        if (cachedClass != receiver.getClass()) {
            System.out.println("change");
            cachedClass = receiver.getClass();
            mh = lookup.findVirtual(cachedClass, name, MethodType.methodType(void.class));
        }
        mh.invoke(receiver);
    }

    public static CallSite bootstrap(MethodHandles.Lookup l, String name, MethodType callSiteType) throws Throwable {
        MonomorphicInlineCache ic = new MonomorphicInlineCache(l, name);
        MethodHandle mh = l.findVirtual(MonomorphicInlineCache.class, "invoke", MethodType.methodType(void.class, Object.class));
        return new ConstantCallSite(mh.bindTo(ic));
    }
}