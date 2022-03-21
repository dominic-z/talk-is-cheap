package domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author dominiczhu
 * @date 2021/9/3 上午11:01
 */
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

}
