package org.talk.is.cheap.java.tool.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressEntity {
    private String province;
    private String city;
    private String detail; // 详细地址
}
