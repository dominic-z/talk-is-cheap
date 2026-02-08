package org.talk.is.cheap.project.free.flow.starter.worker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.talk.is.cheap.project.free.flow.common.utils.YamlUtil;
import org.talk.is.cheap.project.free.flow.starter.worker.config.properties.ZKConfigProperties;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * readme
 */

@Slf4j
public class WorkerStarterEnvironmentPostProcessor implements EnvironmentPostProcessor {


    private final static String WORKER_STARTER_CONFIG_FILENAME = "free-flow-worker-config.yaml";
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource resource = patternResolver.getResource("classpath:/" + WORKER_STARTER_CONFIG_FILENAME);
        if (!resource.exists()) {
            return;
        }
        URL url = null;
        try {
            url = resource.getURL();
        } catch (IOException e) {
            System.err.println("加载worker-starter的yaml文件转url失败");
            e.printStackTrace(System.err);
        }

        Map<String, Object> workerStarterConfig = YamlUtil.loadFileAndFlatten(url);

        // 添加到环境中
        MapPropertySource customSource = new MapPropertySource("myCustomSource", workerStarterConfig);
        environment.getPropertySources().addLast(customSource);

    }

}