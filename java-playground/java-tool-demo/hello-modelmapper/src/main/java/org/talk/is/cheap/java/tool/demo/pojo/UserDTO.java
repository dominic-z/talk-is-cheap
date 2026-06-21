package org.talk.is.cheap.java.tool.demo.pojo;

import lombok.Data;

import java.util.Map;

@Data
public class UserDTO {
    private Long userId; // 对应源对象的id
    private String userName; // 对应源对象的username
    private String email; // 对应源对象的emailAddress
    private int userAge; // 对应源对象的age
    private AddressDTO userAddress; // 嵌套的地址DTO

    private String dummy; // 对象映射过程中，不需要对这个属性进行写入

    private Map<Integer,AccountDTO> accountEntityMap;

    private String notForWrite;

}
