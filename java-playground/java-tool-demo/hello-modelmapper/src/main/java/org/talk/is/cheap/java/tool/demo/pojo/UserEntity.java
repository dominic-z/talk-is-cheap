package org.talk.is.cheap.java.tool.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    private Long id;
    private String username;
    private String emailAddress;
    private int age;
    private AddressEntity address; // 嵌套的地址对象

    private Map<Integer,AccountEntity> accountEntityMap;

    private String dummy; // 对象映射过程中，不需要对这个属性进行读取



}
