package jvm.exception;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ThrowInCatch
 * @date 2021/8/16 下午8:20
 */
public class ThrowInCatch {

    private void throwInCatch(int i) throws Exception {

        try {
            if (i % 2 == 0) {
                throw new Exception("i%2==0");
            }
        } catch (Exception e) {

            if (i == 2) {
                throw new RuntimeException("i==2");
            }

            throw e;
        } finally {
            System.out.println("finally");
        }
    }

    public static void main(String[] args) {
        ThrowInCatch mainObj = new ThrowInCatch();

        try {
            mainObj.throwInCatch(10);
        } catch (Exception e) {
            e.printStackTrace();
        }



        try {
            mainObj.throwInCatch(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
