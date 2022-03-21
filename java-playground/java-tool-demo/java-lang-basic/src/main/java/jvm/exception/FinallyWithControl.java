package jvm.exception;

/**
 * @author dominiczhu
 * @version 1.0
 * @title FinallyWithControl
 * @date 2021/8/16 下午8:33
 */
public class FinallyWithControl {

    private int tryBlock;
    private int catchBlock;
    private int finallyBlock;
    private int methodExit;

    public void test(int start,int end) {
        for (int i = start; i < end; i++) {
            try {
                tryBlock = 0;
                if (i < 50) {
                    continue;
                } else if (i < 80) {
                    break;
                } else {
                    return;
                }
            } catch (Exception e) {
                catchBlock = 1;
            } finally {
                finallyBlock = 2;
            }
        }
        methodExit = 3;
    }

    @Override
    public String toString() {
        return "FinallyWithControl{" +
                "tryBlock=" + tryBlock +
                ", catchBlock=" + catchBlock +
                ", finallyBlock=" + finallyBlock +
                ", methodExit=" + methodExit +
                '}';
    }

    public static void main(String[] args) {
        FinallyWithControl mainObj = new FinallyWithControl();

        mainObj.test(0,20);
        System.out.println(mainObj);

        mainObj = new FinallyWithControl();
        mainObj.test(50,60);
        System.out.println(mainObj);

        mainObj = new FinallyWithControl();
        mainObj.test(90,100);
        System.out.println(mainObj);

        // 只要不断电

    }
}
