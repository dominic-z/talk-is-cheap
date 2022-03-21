package demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dominiczhu
 * @date 2020/8/7 3:23 下午
 */

@Slf4j
public class AnnoSlf4jDemo {


    public static void main(String[] args) {
        // 记录trace级别的信息
        log.trace("log4j2日志输出：This is trace message.");
        // 记录debug级别的信息
        log.debug("log4j2日志输出：This is debug message.");
        // 记录info级别的信息
        log.info("log4j2日志输出：This is info message.");
        // 记录error级别的信息
        log.error("log4j2日志输出：This is error message.");
        log.error("{} log4j2日志输出：This is error message.", "[fuck]", throwRuntimeExp());

    }

    private static Exception throwRuntimeExp() {
        return new RuntimeException("run time");
    }
}
