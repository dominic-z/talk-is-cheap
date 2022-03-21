package demo.enums;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @author dominiczhu
 * @date 2020/8/11 7:49 下午
 */
@Getter
public enum Enum3 {
    E1("E1", "C1"), E2("E2", "C2");

    @JsonValue
    private final String code;
    //只能有唯一的一个JsonValue作为标示
    private final String code2;

    Enum3(String code, String code2) {
        this.code = code;
        this.code2 = code2;
    }


}
