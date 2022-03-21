package beans;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author dominiczhu
 * @date 2020/9/8 6:33 下午
 */
public enum TypeEnumWithValue {
    TYPE1(1, "Type A"), TYPE2(2, "Type 2");

    private Integer id;
    @JsonValue
    private String name;

    private TypeEnumWithValue(Integer id,String name){
        this.id=id;
        this.name=name;
    }


    public String getName() {
        return name;
    }
}
