package geektime.demo.message;

import geektime.demo.domain.enums.Gender;
import geektime.demo.domain.enums.AgeBracket;
import lombok.Data;

/**
 * @author dominiczhu
 * @date 2020/8/11 12:52 下午
 */
@Data
public class HelloPostRequest {
    private AgeBracket ageBracket;
    private Gender gender;
    private String name;
}
