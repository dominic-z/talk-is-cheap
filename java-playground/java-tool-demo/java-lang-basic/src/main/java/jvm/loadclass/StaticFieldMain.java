package jvm.loadclass;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StaticField
 * @date 2021/9/13 下午12:57
 */
public class StaticFieldMain {
    public static  StaticMember staticMember = StaticField.staticMember;


    public static void main(String[] args) {
        System.out.println(staticMember);
    }
}

class StaticField{
    public static  StaticMember staticMember = new StaticMember();

    static {
        System.out.println("statitic field");
    }
}

class StaticMember{
    public StaticMember(){
        System.out.println("initial static member");
    }
}
