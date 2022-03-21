package jvm.exception;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ThrowCachedException
 * @date 2021/8/16 下午7:43
 */
public class ThrowCachedException {

    private void testThrow(int i) {

        RuntimeException exception = new RuntimeException("construct first"); // 异常栈轨迹被指向在了这里，可能造成歧义

        System.out.println("print something");
        if (i % 2 == 0) {
            throw exception;
        }

    }

    public static void main(String[] args) {
        ThrowCachedException throwCachedException = new ThrowCachedException();
        throwCachedException.testThrow(10);
    }

}
