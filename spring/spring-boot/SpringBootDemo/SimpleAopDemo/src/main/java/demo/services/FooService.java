package demo.services;

import org.springframework.stereotype.Service;

/**
 * @author dominiczhu
 * @date 2020/8/12 7:11 下午
 */
@Service
public class FooService implements SupperService {

    public String sayFoo(){
        String res="foo in FooService";
        System.out.println(res);
        return res;
    }
}
