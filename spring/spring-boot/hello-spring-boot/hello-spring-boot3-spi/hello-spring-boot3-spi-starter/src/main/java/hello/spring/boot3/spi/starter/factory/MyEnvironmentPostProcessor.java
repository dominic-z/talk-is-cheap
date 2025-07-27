package hello.spring.boot3.spi.starter.factory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.HashMap;
import java.util.Map;

public class MyEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("----------------通过EnvironmentPostProcessor操作属性，新增一个配置项，并且修改一个server.port配置项");
        // 创建自定义配置源
        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("app.custom.property", "custom-value");
        customProperties.put("server.port", 8888); // 可以覆盖已有配置

        // 添加到环境中
        MapPropertySource customSource = new MapPropertySource("myCustomSource", customProperties);
        environment.getPropertySources().addFirst(customSource);

        // 也可以获取已有的配置
        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        System.out.println("----------------通过EnvironmentPostProcessor读取当前激活的环境: " + activeProfile);

    }
}