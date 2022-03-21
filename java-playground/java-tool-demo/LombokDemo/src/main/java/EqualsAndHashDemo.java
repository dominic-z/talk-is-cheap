import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @title EqualsAndHashDemo
 * @Author dominiczhu
 * @Date 2020/12/11 下午8:47
 * @Version 1.0
 */
public class EqualsAndHashDemo {
    @EqualsAndHashCode
    @Data
    static class BooleanStringKey{
        private boolean aBoolean;
        private String string;
    }

    public static void main(String[] args) {
        Map<BooleanStringKey,Integer> map=new HashMap<>();
        BooleanStringKey key1=new BooleanStringKey();
        key1.setABoolean(true);
        key1.setString("key1");
        map.put(key1,1);

        BooleanStringKey key2=new BooleanStringKey();
        key2.setABoolean(false);
        key2.setString("key1");
        map.put(key2,1);

        System.out.println(map);
    }
}
