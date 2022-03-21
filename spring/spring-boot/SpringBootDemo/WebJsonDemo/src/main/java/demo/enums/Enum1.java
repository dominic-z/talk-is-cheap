package demo.enums;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author dominiczhu
 * @date 2020/8/11 7:49 下午
 */
@Getter
public enum  Enum1 {
    E1("E1"),E2("E2");

    @JsonValue
    private final String code;

    Enum1(String code) {
        this.code = code;
    }
}
