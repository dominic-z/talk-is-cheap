package hello.spring.boot3.spi.starter.factory;

import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 来自https://www.doubao.com/thread/w8c6d7d6764f6e7bf
 */
public class MyPropertySourceLoader implements PropertySourceLoader {

    // 测试出来的，classpath必须得有一个后缀为.json的文件，这个PropertySourceLoader才能触发。
    // 支持的文件扩展名
    @Override
    public String[] getFileExtensions() {
        return new String[]{"json"};
    }

    // 加载配置文件并转换为PropertySource
    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        System.out.println("-------------load json");
        // 1. 读取资源内容（简化示例，实际需处理IO异常）
        String jsonContent = new String(resource.getInputStream().readAllBytes());

        // 2. 解析JSON为Map（可使用Jackson等库）
        Map<String, Object> properties = parseJsonToMap(jsonContent);

        // 3. 创建并返回PropertySource
        return List.of(new MapPropertySource(name, properties));
    }

    // 实际项目中使用Jackson等库解析JSON
    private Map<String, Object> parseJsonToMap(String json) {
        // 简化实现，实际需处理解析逻辑
        return Map.of("property.source.key", "value from json");
    }
}