package section1.chapter1;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Singleton
 * @date 2021/6/21 上午10:00
 */

public class Singleton {
    static Singleton instance;
    static Singleton getInstance(){
        if (instance == null) {
            synchronized(Singleton.class) {
                if (instance == null)
                    instance = new Singleton();
            }
        }
        return instance;
    }

}