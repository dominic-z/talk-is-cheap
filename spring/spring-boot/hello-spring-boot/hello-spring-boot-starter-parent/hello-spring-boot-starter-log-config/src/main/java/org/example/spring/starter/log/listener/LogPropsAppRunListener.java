package org.example.spring.starter.log.listener;

import org.example.spring.starter.log.Loggers;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author dominiczhu
 * @version 1.0
 * @title AppRunListener
 * @date 2022/2/13 10:15 下午
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogPropsAppRunListener implements SpringApplicationRunListener {


    public static final String SYSTEM_PROPERTIES = "system.properties";

    public LogPropsAppRunListener(SpringApplication application, String[] args) {
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
//        SpringApplicationRunListener.super.starting(bootstrapContext);

        final Properties applicationProperties = new Properties();
        // 读取当前应用的system.properties文件
        final URL url = this.getClass().getClassLoader().getResource(SYSTEM_PROPERTIES);
        if (url != null) {
            try (FileInputStream fileInputStream = new FileInputStream(Paths.get(url.getPath()).toFile())) {
                applicationProperties.load(fileInputStream);
            } catch (Exception e) {
                Loggers.ERROR_LOG.error("cannot load system.properties");
            }

        }
        for (String key : applicationProperties.stringPropertyNames()) {
            System.setProperty(key, applicationProperties.getProperty(key));

        }

        final String LOG_DIR = "log.dir";
        if (!System.getProperties().containsKey(LOG_DIR)) {
            System.setProperty(LOG_DIR, System.getProperty("user.dir") + "/logs");
        }

    }
}
