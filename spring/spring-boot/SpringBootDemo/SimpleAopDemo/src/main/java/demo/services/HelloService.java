package demo.services;

import org.springframework.stereotype.Service;

/**
 * @author dominiczhu
 * @date 2020/8/12 7:10 下午
 */
@Service
public class HelloService {
    public String sayHello() {
        String res = "hello in HelloService";
        System.out.println(res);
        return res;
    }

    public String sayHelloForSelf() {
        String res = "helloSelf in HelloService";
        System.out.println(res);
        return res;
    }

    public String callSelf() {
        return sayHelloForSelf();
    }
}
