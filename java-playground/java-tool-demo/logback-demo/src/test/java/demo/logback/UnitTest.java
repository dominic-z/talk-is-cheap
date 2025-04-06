package demo.logback;


import demo.logback.util.Loggers;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;


// 在test里运行不太一样，在test里运行xml配置中的file日志的根目录是当前子模块，而如果放在正常代码里执行，根目录在父工程里。需要注意。
public class UnitTest {


    @Test
    public void logDemo1() throws InterruptedException {
        Loggers.CONSOLE.info("console info");
        Loggers.CONSOLE.debug("console debug"); // 不会输出
        Loggers.CONSOLE.warn("console warn");
        Loggers.ROOT.info("abc");
        Thread.sleep(2000);
        Loggers.CONSOLE.error("console errror", new RuntimeException());
    }





    @Test
    public void logFile() throws InterruptedException {

        Loggers.FILE.info("file info");
        Loggers.TIME_BASED_ROLLING_FILE.info("rolling_file info1");
        Thread.sleep(2000);
        Loggers.TIME_BASED_ROLLING_FILE.info("rolling_file info2");
    }

    @Test
    public void loopLog() throws Exception {

        for (int i = 0; i < 100000; i++) {
            Loggers.CONSOLE.info("console info {}", i);
            Thread.sleep(100);
            Loggers.TIME_BASED_ROLLING_FILE.info("time_based_rolling_file info {}", i);
            Loggers.SIZE_BASED_ROLLING_FILE.info("size_based_rolling_file info {}", i);
            Loggers.SIZE_AND_TIME_BASED_ROLLING_FILE.info("TIME_AND_SIZE_BASED_ROLLING_FILE info {}", i);

        }

    }


    @Test
    public void useCustomLog(){
        Loggers.CUSTOM_CONFIG_LOG.info("custom-config");
        Loggers.CONSOLE.info("common config");
        Loggers.ROOT.info("common config1");
        Loggers.ROOT.info("common config2");
        Loggers.ROOT.error("common config2",new RuntimeException("aaa"));
    }


    @Test
    public void logFilter() {
        Loggers.THRESHOLD_FILTER_CONSOLE.trace("FILTER_CONSOLE trace"); // 不会输出
        Loggers.THRESHOLD_FILTER_CONSOLE.info("FILTER_CONSOLE info"); // 不会输出
        Loggers.THRESHOLD_FILTER_CONSOLE.warn("FILTER_CONSOLE warn");
        Loggers.THRESHOLD_FILTER_CONSOLE.error("FILTER_CONSOLE error", new Exception());

        Loggers.MULTI_FILTER_CONSOLE.trace("MULTI_FILTER_CONSOLE trace"); // 不会输出
        Loggers.MULTI_FILTER_CONSOLE.info("MULTI_FILTER_CONSOLE info"); // 会输出
        Loggers.MULTI_FILTER_CONSOLE.warn("MULTI_FILTER_CONSOLE warn");
        Loggers.MULTI_FILTER_CONSOLE.error("MULTI_FILTER_CONSOLE error", new Exception());
    }


    @Test
    public void testMDCLogger(){
//        在多线程环境中，MDC 是基于线程的，每个线程都有自己独立的 MDC 副本。
        MDC.put("myTraceId","aabbccdd");

        new Thread(()->{
            MDC.put("myTraceId","aabbccddee");
        });
        Loggers.MDC_CONSOLE.info("show my traceID");
    }
}
