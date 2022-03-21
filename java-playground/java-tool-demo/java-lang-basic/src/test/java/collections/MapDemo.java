package collections;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MapDemo
 * @date 2021/11/13 下午4:01
 */
public class MapDemo {

    static class Key {

        int i;

        public Key(int i) {
            this.i = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return i == key.i;
        }

        @Override
        public int hashCode() {
            return i % 2;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "i=" + i +
                    '}';
        }
    }

    @Test
    public void testHashAndEquels() {

        Map<Key, Integer> map = new HashMap<>();

        map.put(new Key(1), 1);
        map.put(new Key(3), 1);
        System.out.println(map);

    }
}
