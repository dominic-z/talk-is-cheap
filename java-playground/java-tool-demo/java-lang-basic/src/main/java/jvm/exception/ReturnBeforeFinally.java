package jvm.exception;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ReturnBeforeFinally
 * @date 2021/8/16 下午8:57
 */
public class ReturnBeforeFinally {
    private int tryBlock;
    private int catchBlock;
    private int finallyBlock;
    private int methodExit;

    public void test() {
        try {
            tryBlock = 0;
            return;
        } catch (Exception e) {
            catchBlock = 1;
        } finally {
            finallyBlock = 2;
        }
        methodExit = 3;
    }

    @Override
    public String toString() {
        return "ReturnBeforeFinally{" +
                "tryBlock=" + tryBlock +
                ", catchBlock=" + catchBlock +
                ", finallyBlock=" + finallyBlock +
                ", methodExit=" + methodExit +
                '}';
    }

    public static void main(String[] args) {

        ReturnBeforeFinally mainObj = new ReturnBeforeFinally();

        mainObj.test();
        System.out.println(mainObj);

    }
}
