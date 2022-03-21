package com.example.components;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/18 4:58 下午
 */
@Component("protoApple")
@Scope("prototype")
@Data
public class ProtoApple {
    @Value("protoAppleName")
    private String protoAppleName;
}
