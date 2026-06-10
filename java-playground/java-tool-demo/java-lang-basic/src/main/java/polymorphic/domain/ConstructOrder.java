package polymorphic.domain;

public class ConstructOrder {

    private static class Parent {
        static { System.out.println("1. Parent static block"); }
        Parent() { System.out.println("4. Parent constructor"); }
        { System.out.println("3. Parent instance block"); }
        int x = print("2. Parent field x");
    }

    private static class Child extends Parent {
        static { System.out.println("5. Child static block"); }  // ❌ 不是第5！
        int y = print("6. Child field y");                       // ❌ 不是第6！
        { System.out.println("7. Child instance block"); }
        Child() { System.out.println("8. Child constructor"); }
    }

    public static int print(String s){
        System.out.println(s);
        return s.length();
    }

    public static void main(String[] args) {
        new Child();
    }
}
