package jvm.understandjvm.chapter2.seciton3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title JavaVMStackSOF
 * @date 2021/10/14 下午9:11
 *
 * -Xss128k
 */
public class JavaVMStackSOF {
    private int stackLength = 1;
    public void stackLeak() {
        stackLength++;
        stackLeak();
    }
    public static void main(String[] args) throws Throwable {
        JavaVMStackSOF oom = new JavaVMStackSOF();
        try {
            oom.stackLeak();
        } catch (Throwable e) {
            System.out.println("stack length:" + oom.stackLength);
            throw e;
        }
    }
}
