package geektime.demo.converters;

import geektime.demo.domain.enums.AgeBracket;
import org.springframework.core.convert.converter.Converter;

/**
 * @author dominiczhu
 * @version 1.0
 * @title StringToGenderEnumConverter
 * @date 2021/9/3 上午10:33
 */

public class AgeBracketEnumConverter implements Converter<String, AgeBracket> {
    @Override
    public AgeBracket convert(String s) {
        for (AgeBracket e : AgeBracket.values()) {
            if (e.getAgeBracketDescr().equals(s)) {
                return e;
            }
        }
        return null;
    }
}
