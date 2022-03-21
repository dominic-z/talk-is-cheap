package demo.message;

import demo.enums.Enum1;
import demo.enums.Enum2;
import demo.enums.Enum3;
import lombok.Data;

/**
 * @author dominiczhu
 * @date 2020/8/11 7:59 下午
 */
@Data
public class RequestWithEnum {
    private Enum1 e1;
    private Enum2 e2;
    private Enum3 e3;
}
