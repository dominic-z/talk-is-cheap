package org.talk.is.cheap.distributed.etcd.java;

import org.junit.jupiter.api.Test;
import org.talk.is.cheap.distributed.etcd.java.discovery.Register;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RegisterTest
 * @date 2022/3/28 3:28 下午
 */
public class RegisterTest {

    @Test
    public void registerTest(){

        Register register = new Register("http://localhost:2379");
        String key = "/web/node0";
        String value = "localhost:7999";
//        try {
//            register.put(key, value);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            register.putWithLease(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
