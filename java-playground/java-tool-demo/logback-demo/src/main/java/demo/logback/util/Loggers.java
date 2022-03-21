package demo.logback.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Loggers
 * @date 2022/2/13 3:04 下午
 */
public class Loggers {

    public static final Logger CONSOLE = LoggerFactory.getLogger("console");
    public static final Logger ROOT = LoggerFactory.getLogger("root");
    public static final Logger FILE = LoggerFactory.getLogger("file");
    public static final Logger TIME_BASED_ROLLING_FILE = LoggerFactory.getLogger("time_based_rolling_file");
    public static final Logger SIZE_BASED_ROLLING_FILE = LoggerFactory.getLogger("size_based_rolling_file");
    public static final Logger SIZE_AND_TIME_BASED_ROLLING_FILE = LoggerFactory.getLogger("size_and_time_based_rolling_file");
    public static final Logger FILTER_CONSOLE = LoggerFactory.getLogger("filter_console");
}
