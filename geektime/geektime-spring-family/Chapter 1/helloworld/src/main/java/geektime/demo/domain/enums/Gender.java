package geektime.demo.domain.enums;

/**
 * 用于演示spring-boot对enum的映射，就如果说传入的HelloPostRequest的gender字段为descr里的值，就比如male和female，而并非Gender的字面值
 * 希望把他映射为Gender枚举，可以通过如下两种方式，即使用JsonValue或者JsonCreator
 *
 * @author dominiczhu
 * @date 2020/8/11 3:41 下午
 */

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("male"),
    FEMALE("female");

    @JsonValue
    private final String descr;

    Gender(String descr) {
        this.descr = descr;
    }


    public String getDescr() {
        return descr;
    }


    //    @JsonCreator
    public static Gender create(String descr) {
        System.out.println("使用jsoncreate");
        for (Gender gender : Gender.values()) {
            if (gender.descr.equals(descr)) {
                return gender;
            }
        }
        return null;
    }
}

