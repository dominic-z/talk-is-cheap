package jvm.understandjvm.chapter4.section3;


/**


 通过jvisualvm启动
记得vm 参数加上 -Xshare:off -Xverify:none
 点完profiling之后记得等着
 * @author dominiczhu
 * @version 1.0
 * @title JHSDB_TestCase
 * @date 2021/10/17 下午10:28
 */
public class VMVisual_Profiling {

    static class Test {
        static ObjectHolder staticObj = new ObjectHolder();
        static ObjectHolder staticObj2 = staticObj;
        ObjectHolder instanceObj = new ObjectHolder();

        void foo() throws InterruptedException {
            ObjectHolder localObj = new ObjectHolder();
            System.out.println("done"); // 这里设一个断点

        }
    }

    private static class ObjectHolder {
    }

    public static void main(String[] args) throws InterruptedException {
        Test test = new VMVisual_Profiling.Test();
        test.foo();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<2000;i++) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("running");
                }
            }
        });

        Thread.sleep(600000);

    }

}
