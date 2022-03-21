package jvm.loadclass;

/**
 * @author dominiczhu
 * @version 1.0
 * @title LoadClass
 * @date 2021/8/13 上午10:38
 */
public class LoadSingletonClass {
    private LoadSingletonClass() {
    }

    private static class LazyHolder {
        static final LoadSingletonClass INSTANCE = new LoadSingletonClass();

        static {
            System.out.println("LazyHolder.");
        }
    }

    public static Object getInstance(boolean flag) {
        if (flag) return new LazyHolder[2];
        return LazyHolder.INSTANCE;
    }

    public static void main(String[] args) {
        getInstance(true);
        System.out.println("----");
        getInstance(false);
    }
}
