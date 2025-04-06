package demo.logback.util;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Loggers
 * @date 2022/2/13 3:04 下午
 */
public class Loggers {


    static {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);

            // 配置多个配置文件的方式1
            // 清除之前的配置：然后手动配置
            context.reset();
            // 指定配置文件路径
            URL resource1 = Loggers.class.getClassLoader().getResource("custom-logback.xml");
            assert resource1 != null;
            URL resource2 = Loggers.class.getClassLoader().getResource("logback.xml");
            assert resource2 != null;
            // configurator.doConfigure支持配置多个
            configurator.doConfigure(new File(resource1.toURI()));
            configurator.doConfigure(new File(resource2.toURI()));

            // 配置多个xml配置的方式2
            // 不清除之前的配置，即保留默认的logback.xml，在原有基础上新增
//            URL resource1 = Loggers.class.getClassLoader().getResource("custom-logback.xml");
//            assert resource1 != null;
//            configurator.doConfigure(new File(resource1.toURI()));

        } catch (JoranException je) {
            je.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        CUSTOM_CONFIG_LOG = LoggerFactory.getLogger("custom_config");
    }

    public static final Logger CONSOLE = LoggerFactory.getLogger("console");
    public static final Logger ROOT = LoggerFactory.getLogger("root");
    public static final Logger FILE = LoggerFactory.getLogger("file");
    public static final Logger TIME_BASED_ROLLING_FILE = LoggerFactory.getLogger("time_based_rolling_file");
    public static final Logger SIZE_BASED_ROLLING_FILE = LoggerFactory.getLogger("size_based_rolling_file");
    public static final Logger SIZE_AND_TIME_BASED_ROLLING_FILE = LoggerFactory.getLogger(
            "size_and_time_based_rolling_file");
    public static final Logger THRESHOLD_FILTER_CONSOLE = LoggerFactory.getLogger("threshold_filter_console");
    public static final Logger MULTI_FILTER_CONSOLE = LoggerFactory.getLogger("multi_filter_console");
    public static final Logger MDC_CONSOLE = LoggerFactory.getLogger("mdc_console");

    public static final Logger CUSTOM_CONFIG_LOG;
}
