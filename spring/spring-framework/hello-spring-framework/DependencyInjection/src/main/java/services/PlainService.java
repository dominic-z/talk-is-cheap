package services;

import org.springframework.stereotype.Service;

/**
 * @author dominiczhu
 * @version 1.0
 * @title PlainService
 * @date 2021/3/23 下午7:39
 */
@Service
public class PlainService {


    public void say(){
        System.out.println("I am banana");
    }

    @Override
    public String toString() {
        return "PlainService{}";
    }
}
