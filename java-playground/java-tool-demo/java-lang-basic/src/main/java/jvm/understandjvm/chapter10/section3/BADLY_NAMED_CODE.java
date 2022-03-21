package jvm.understandjvm.chapter10.section3;

/**
 *
 * 进入到java-lang-basic/src/main/java这个目录，执行下面的代码
 * javac  jvm/understandjvm/chapter10/section3/NameChecker.java jvm/understandjvm/chapter10/section3/NameCheckProcessor.java
 * javac -classpath %CLASSPATH%:/Users/dominiczhu/work/learn/java/java-tool-demo/java-lang-basic/src/main/java/
 * -processor  jvm
 * .understandjvm.chapter10.section3.NameCheckProcessor jvm/understandjvm/chapter10/section3/BADLY_NAMED_CODE.java
 *
 * @author dominiczhu
 * @version 1.0
 * @title BADLY_NAMED_CODE
 * @date 2021/10/24 下午6:07
 */
public class BADLY_NAMED_CODE {
    enum colors {
        red, blue, green;
    }

    static final int _FORTY_TWO = 42;
    public static int NOT_A_CONSTANT = _FORTY_TWO;
    protected void BADLY_NAMED_CODE() {
        return;
    }
    public void NOTcamelCASEmethodNAME() {
        return;
    }

}
