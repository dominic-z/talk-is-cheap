package generic;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @title Wildecard
 * @Author dominiczhu
 * @Date 2020/12/24 下午12:52
 * @Version 1.0
 */
public class Wildcard {

    public void testGet(List<?> wildcards) {
        System.out.println(wildcards.get(0));
    }

    @Test
    public void testGet() {
        List<String> strings = new ArrayList<>();
        strings.add(null);
        strings.add("abc");
        testGet(strings);
    }
}
