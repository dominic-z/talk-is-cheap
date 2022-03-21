package demo.domain.pojo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dominiczhu
 * @date 2020/8/12 11:16 上午
 */
@Component(value = "singleBanana1")
@Data
public class Banana1 {
    @Value("${banana.name}")
    private String name;
}
