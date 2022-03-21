import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dominiczhu
 * @date 2020/8/7 3:23 下午
 */

public class Slf4jDemo {

    private static Logger logger = LoggerFactory.getLogger("mylog");

    private static Logger classLogger = LoggerFactory.getLogger(Slf4jDemo.class);

    public static void main(String[] args) {
        System.out.println("==============服从自定义命名的logger的配置");
        // 记录trace级别的信息
        logger.trace("log4j2日志输出：This is trace message.");
        // 记录debug级别的信息
        logger.debug("log4j2日志输出：This is debug message.");
        // 记录info级别的信息
        logger.info("log4j2日志输出：This is info message.");
        // 记录error级别的信息
        logger.error("log4j2日志输出：This is error message.");
        logger.error("{} log4j2日志输出：This is error message.", "[fuck]", throwRuntimeExp());

        System.out.println("==============类logger服从root的配置");
        // 记录trace级别的信息
        classLogger.trace("log4j2日志输出：This is trace message.");
        // 记录debug级别的信息
        classLogger.debug("log4j2日志输出：This is debug message.");
        // 记录info级别的信息
        classLogger.info("log4j2日志输出：This is info message.");
        // 记录error级别的信息
        classLogger.error("log4j2日志输出：This is error message.");
    }

    private static Exception throwRuntimeExp() {
        return new RuntimeException("run time");
    }
}
