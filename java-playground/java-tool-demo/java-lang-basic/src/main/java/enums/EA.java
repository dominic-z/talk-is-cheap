package enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @date 2020/12/8 下午4:23
 */
public enum EA {
    EA1("ea1",1),
    EA2("ea2",2);

    EA(String name,int i){
        this.name=name;
        this.i=i;
    }
    private String name;
    private int i;

    private final static Map<String,EA> NAME_MAP = new HashMap<>();
    static {
        for (EA value : EA.values()) {
            NAME_MAP.put(value.name,value);
        }
    }

    public static EA getByName(String name){
        return NAME_MAP.get(name);
    }

}
