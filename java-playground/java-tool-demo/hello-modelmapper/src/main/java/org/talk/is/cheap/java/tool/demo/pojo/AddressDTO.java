package org.talk.is.cheap.java.tool.demo.pojo;

import lombok.Data;

@Data
public class AddressDTO {
    private String region; // 对应源对象的province
    private String cityName; // 对应源对象的city
    private String fullAddress; // 对应源对象的detail
}
