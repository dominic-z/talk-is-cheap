package opJavaClass;

import java.util.List;

/**
 * @author dominiczhu
 * @date 2020/11/20 下午2:14
 */
public class JavaCollectionsDemo {

    private List<String> stringList;
    public void consumeList(List<String> stringList){
        this.stringList=stringList;
        System.out.println(stringList);
    }

    public static void main(String[] args) {
        new JavaCollectionsDemo();
    }
}
