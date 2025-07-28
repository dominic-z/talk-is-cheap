package hello.spring.boot3.spi.starter.starter.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("starter.configuration.properties")
@Data
public class StarterConfigProperties {
    private String k;

}
