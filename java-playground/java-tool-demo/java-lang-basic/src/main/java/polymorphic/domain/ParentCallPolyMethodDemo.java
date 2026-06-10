package polymorphic.domain;

import java.util.concurrent.ThreadLocalRandom;

public class ParentCallPolyMethodDemo {

    private static class Father {

        protected void fatherCall() {
            say();
        }

        protected void say() {
            System.out.println("father say");
        }
    }

    private static class Child extends Father {

        void childCall() {
            super.fatherCall();
        }

        @Override
        protected void say() {
            System.out.println("child say");
        }
    }

    public static void main(String[] args) {
        new Child().childCall();
        ThreadLocalRandom.current().nextInt(20);
    }
}
