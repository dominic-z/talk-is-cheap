package autowired;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import services.PlainService;

import javax.annotation.PostConstruct;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SomeUtil
 * @date 2021/3/23 下午7:37
 */

@Component
public class SomeUtil {

    @Autowired
    private PlainService autowiredService;

    private static PlainService plainService;

    @PostConstruct
    public void init() {
        System.out.println("SomeUtil的PostConstruct方法执行");
        System.out.println(this.autowiredService);
        SomeUtil.plainService=this.autowiredService;
    }

    public static void show() {
        System.out.println("SomeUtil的show");
    }

    public static void useService(){
        plainService.say();
    }

}
