package jvm.understandjvm.chapter4.section4;

/**
 * https://github.com/liuzhengyang/hsdis
 * -XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly -Xcomp -XX:CompileCommand=dontinline,*Bar.sum -XX:CompileCommand=compileonly,*Bar.sum
 * @author dominiczhu
 * @version 1.0
 * @title Bar
 * @date 2021/10/18 下午7:28
 */
public class Bar {
    int a = 1;
    static int b = 2;
    public int sum(int c) {
        return a + b + c;
    }
    public static void main(String[] args) {
        new Bar().sum(3);
    }
}
