package resources.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.Getter;


/**
 * 这个请求对象用于演示如何将请求体中的一些信息直接转换为enum对象
 *
 *
 * {
 *   "e1": "E1",
 *   "e2": "E1",
 *   "e3": "E2"
 * }
 */
@Data
public class EnumsReq {
    @Getter
    public enum  Enum1 {
        E1("E1"),E2("E2");

        /**
         * @JsonValue 注解表明，如果请求体中与enum1对应的字段是一个字符串，将这个字符串与code比较，相同的就是对应的enum
         */
        @JsonValue
        private final String code;

        Enum1(String code) {
            this.code = code;
        }
    }

    @Getter
    public enum Enum2 {
        E1("E1"),E2("E2");


        private final String code;

        Enum2(String code) {
            this.code = code;
        }

        /**
         * 通过@JsonCreator，根据请求体中的与enum2对应的数据转换为enum2对象
         * @param codeOrNameOrOrdinal
         * @return
         */
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


    @Getter
    public enum Enum3 {
        E1("E1", "C1"), E2("E2", "C2");

        @JsonValue
        private final String code;
        //只能有唯一的一个JsonValue作为标示，不能指定两个
        private final String code2;

        Enum3(String code, String code2) {
            this.code = code;
            this.code2 = code2;
        }


    }



    private Enum1 e1;
    private Enum2 e2;
    private Enum3 e3;

}
