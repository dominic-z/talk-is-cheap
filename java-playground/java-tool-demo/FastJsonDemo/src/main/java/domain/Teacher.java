package domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.ToString;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Teacher
 * @date 2021/6/25 下午5:03
 */
@ToString(of = {"name"})
public class Teacher {

    private String name="xy";

    public String getName() {
        return name;
    }

    @JSONField(serialize = false)
    public String getLastName(){
        return "zhu";
    }


}
