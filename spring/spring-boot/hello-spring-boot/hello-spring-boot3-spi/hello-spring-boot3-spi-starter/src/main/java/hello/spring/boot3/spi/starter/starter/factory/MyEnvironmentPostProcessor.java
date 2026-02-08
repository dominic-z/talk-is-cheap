package hello.spring.boot3.spi.starter.starter.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * readme
 */

@Slf4j
public class MyEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("----------------通过EnvironmentPostProcessor操作属性，新增一个配置项，并且修改一个server.port配置项");
        // 创建自定义配置源
        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("app.custom.property", "custom-value");
        customProperties.put("server.port", 8888); // 可以覆盖已有配置


        customProperties.put("starter.configuration.properties.k", readStarterConfiguration()); // 用来给StarterConfigProperties用，假装是读取了什么xxx.yaml文件吧。

        // 添加到环境中
        MapPropertySource customSource = new MapPropertySource("myCustomSource", customProperties);
        environment.getPropertySources().addFirst(customSource);

//        测试能不能
        // 设置spring的日志级别为 DEBUG
//        可以https://www.qianwen.com/share/chat/507898bdfa9d45dfaadfd60cefa24f14
        Map<String, Object> loggingProps = new HashMap<>();
        loggingProps.put("logging.level.root", "INFO");

        // 添加到 Environment（低优先级，允许主应用覆盖）
        environment.getPropertySources().addLast(
                new MapPropertySource("customLoggingLevels", loggingProps)
        );

        // 也可以获取已有的配置
        String activeProfile = environment.getProperty("spring.profiles.active", "default");
        System.out.println("----------------通过EnvironmentPostProcessor读取当前激活的环境: " + activeProfile);

    }

    private String readStarterConfiguration() {
        System.out.println("----------------读取 starter.configuration.properties");
        URL resource = MyEnvironmentPostProcessor.class.getClassLoader().getResource("starter.configuration.properties");
        if (resource == null) {
            System.out.println("----------------读取 starter.configuration.properties失败");
            return "file not found";
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(resource.getPath()))) {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}