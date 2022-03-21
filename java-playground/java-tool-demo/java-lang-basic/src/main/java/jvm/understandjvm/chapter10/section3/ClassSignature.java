package jvm.understandjvm.chapter10.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ClassSignature
 * @date 2021/10/24 下午5:18
 */
public class ClassSignature<T> {

    public void show(T t){
        System.out.println(t);
    }

    public static void main(String[] args) {
        final ClassSignature<String> classSignature = new ClassSignature<>();

    }

}
