package demo.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

/**
 * @author dominiczhu
 * @date 2020/8/11 7:49 下午
 */
@Getter
public enum Enum2 {
    E1("E1"),E2("E2");


    private final String code;

    Enum2(String code) {
        this.code = code;
    }

    @JsonCreator
    public static Enum2 create(String codeOrNameOrOrdinal){
        try{
            int i=Integer.parseInt(codeOrNameOrOrdinal);
            return Enum2.class.getEnumConstants()[i];
        }catch (Exception e1){
            return Enum2.valueOf(codeOrNameOrOrdinal);
        }
    }
}
