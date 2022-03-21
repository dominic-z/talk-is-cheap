/**
 * @author dominiczhu
 * @date 2020/8/7 2:43 下午
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class log4j2Test {

    private static Logger rootLogger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private static Logger logger = LogManager.getLogger("custom.log");

    public static void main(String[] args) {

        // 记录trace级别的信息
        rootLogger.trace("log4j2日志输出：This is trace message.");
        // 记录debug级别的信息
        rootLogger.debug("log4j2日志输出：This is debug message.");
        // 记录info级别的信息
        rootLogger.info("log4j2日志输出：This is info message.");
        // 记录error级别的信息
        rootLogger.error("log4j2日志输出：This is error message.");

        logger.info("mylog");
    }
}