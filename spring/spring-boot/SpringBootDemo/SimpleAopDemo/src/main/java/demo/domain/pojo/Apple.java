package demo.domain.pojo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/12 10:20 上午
 */
@Component
@Data
public class Apple {
    @Value("${apple.name}")
    private String name;
}
