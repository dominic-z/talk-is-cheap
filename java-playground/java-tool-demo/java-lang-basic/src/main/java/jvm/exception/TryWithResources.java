package jvm.exception;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TryWithResources
 * @date 2021/8/16 下午8:26
 */
public class TryWithResources {

    private static class Foo implements AutoCloseable {
        private final String name;

        public Foo(String name) {
            this.name = name;
        }

        @Override
        public void close() {
            throw new RuntimeException(name);
        }
    }


    private void closeFooWithTWR() {
        try (Foo foo0 = new Foo("Foo0"); // try-with-resources
             Foo foo1 = new Foo("Foo1");
             Foo foo2 = new Foo("Foo2")) {
            throw new RuntimeException("Initial"); // 原本的异常不会丢失
        }
    }


    private void closeFooWithTryCatch() {
        Foo foo0 = new Foo("Foo0"); // try-with-resources
        Foo foo1 = new Foo("Foo1");
        Foo foo2 = new Foo("Foo2");
        try {
            throw new RuntimeException("Initial");
        }finally {
            foo0.close();// 原本的异常会丢失
            foo1.close();
            foo2.close();
        }
    }

    public static void main(String[] args) {
        TryWithResources mainObj = new TryWithResources();
        try {
            mainObj.closeFooWithTWR();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            mainObj.closeFooWithTryCatch();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
