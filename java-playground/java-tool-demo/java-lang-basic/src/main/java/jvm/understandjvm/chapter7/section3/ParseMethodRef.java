package jvm.understandjvm.chapter7.section3;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ParseMethodRef
 * @date 2021/10/20 下午11:26
 */
public class ParseMethodRef {

    void show(){
        final AnotherC anotherC = new AnotherC();
        anotherC.show();
        int i=anotherC.i;
    }
}

class AnotherC{
    int i;
    void show(){}
}
