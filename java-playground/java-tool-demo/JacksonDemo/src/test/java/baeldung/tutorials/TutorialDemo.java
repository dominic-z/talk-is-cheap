package baeldung.tutorials;

import beans.BeanWithEnum;
import beans.ExtendableBean;
import beans.TypeEnumWithValue;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author dominiczhu
 * @date 2020/9/8 6:35 下午
 */
public class TutorialDemo {
    @Test
    public void whenSerializingUsingJsonAnyGetter_thenCorrect()
            throws JsonProcessingException {

        ExtendableBean bean = new ExtendableBean("My bean");
        bean.add("attr1", "val1");
        bean.add("attr2", "val2");

        String result = new ObjectMapper().writeValueAsString(bean);
        System.out.println(result);
    }

    @Test
    public void whenSerializingUsingJsonValue_thenCorrect()
            throws JsonParseException, IOException {
        String enumAsString = new ObjectMapper()
                .writeValueAsString(TypeEnumWithValue.TYPE1);

        System.out.println(enumAsString);
        BeanWithEnum beanWithEnum = new ObjectMapper().readValue("{\"e\":\"Type A\"}",
                BeanWithEnum.class);
        System.out.println(beanWithEnum.getE());
    }
}