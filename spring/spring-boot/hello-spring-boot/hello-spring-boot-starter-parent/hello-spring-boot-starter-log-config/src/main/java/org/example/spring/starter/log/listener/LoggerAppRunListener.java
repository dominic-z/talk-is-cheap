package org.example.spring.starter.log.listener;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.example.spring.starter.log.Loggers;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

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
public class LoggerAppRunListener implements SpringApplicationRunListener {


    public static final String SYSTEM_PROPERTIES = "system.properties";

    public LoggerAppRunListener(SpringApplication application, String[] args) {
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
//        SpringApplicationRunListener.super.starting(bootstrapContext);

        final Properties applicationProperties = new Properties();
        // 读取当前应用的system.properties文件，其中一些变量将用于logback-spring.xml中，例如hello.starter.log.mybatis.level
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

        // 可以看到logback-spring.xml里有一个配置项目${log.dir}，如果在项目的resources中的system.properties文件中没有配置log.dir，
        // 那么就在环境变量里设置一个默认的log.dir
        final String LOG_DIR = "log.dir";
        if (!System.getProperties().containsKey(LOG_DIR)) {
            System.setProperty(LOG_DIR, System.getProperty("user.dir") + "/logs");
        }

    }



    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

        // 其实这段配置你放在哪都行，放在contextPrepared或者EnvironmentPrepared其实都行
        try {

            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();


            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
//            不要执行lc.reset()，这样还能保证默认的logback.xml配置被正常加载进来。
//            lc.reset();
            // 读取本jar包下的文件
            final String config = "logback-spring.xml";
            ClassPathResource resource = new ClassPathResource(config);
            configurator.doConfigure(resource.getInputStream());

//            或者手动加载logback.xml
//            final String defaultLogbackConfig = "logback.xml";
//            ClassPathResource defaultResource = new ClassPathResource(defaultLogbackConfig);
//            configurator.doConfigure(defaultResource.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("logback config error", e);
        }
    }
}
