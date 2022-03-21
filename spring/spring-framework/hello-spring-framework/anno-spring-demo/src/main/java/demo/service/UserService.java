package demo.service;

import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * @author dominiczhu
 * @version 1.0
 * @title UserService
 * @date 2021/9/14 下午5:13
 */
@Component
public class UserService {
    // 成员变量:
    public final ZoneId zoneId = ZoneId.systemDefault();

    // 构造方法:
    public UserService() {
        System.out.println("UserService(): init...");
        System.out.println("UserService(): zoneId = " + this.zoneId);
    }

    // public方法:
    public ZoneId getZoneId() {
        return zoneId;
    }

    // public final方法:
    public final ZoneId getFinalZoneId() {
        return zoneId;
    }
}