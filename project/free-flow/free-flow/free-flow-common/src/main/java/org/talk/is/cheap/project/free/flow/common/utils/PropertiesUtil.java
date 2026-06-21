package org.talk.is.cheap.project.free.flow.common.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    public static Properties readFromAppClassPath(String filepath) throws IOException {
        Properties properties = new Properties();
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource(filepath);
        if (!resource.exists()) {
            throw new FileNotFoundException(filepath + " doesn't exist");
        }
        try (InputStream inputStream = resource.getInputStream()) {
            // 加载输入流到Properties对象
            properties.load(inputStream);
        }
        return properties;
    }
}
