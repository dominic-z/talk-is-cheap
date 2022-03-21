package demo.logback;

import demo.logback.util.Loggers;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TryLog
 * @date 2022/2/13 3:07 下午
 */
public class TryLog {
    public static void main(String[] args) throws Exception {
        Loggers.CONSOLE.info("console info");
        Loggers.ROOT.info("abc");
        logError();

//        Loggers.FILTER_CONSOLE.info("FILTER_CONSOLE info");
//        Loggers.FILTER_CONSOLE.warn("FILTER_CONSOLE warn");
//        Loggers.FILTER_CONSOLE.error("FILTER_CONSOLE error",new Exception());
//
//
//        Loggers.FILE.info("file info");
//        Loggers.TIME_BASED_ROLLING_FILE.info("rolling_file info");
//        Thread.sleep(2000);
//        Loggers.TIME_BASED_ROLLING_FILE.info("rolling_file info");
//
//        loopLog();

    }

    private static void logError(){
        Loggers.CONSOLE.error("console errror",new RuntimeException());
    }

    private static void loopLog() throws Exception {

        for (int i = 0; i < 100000; i++) {
            Loggers.CONSOLE.info("console info {}", i);
            Thread.sleep(100);
            Loggers.TIME_BASED_ROLLING_FILE.info("time_based_rolling_file info {}", i);
            Loggers.SIZE_BASED_ROLLING_FILE.info("size_based_rolling_file info {}", i);
            Loggers.SIZE_AND_TIME_BASED_ROLLING_FILE.info("TIME_AND_SIZE_BASED_ROLLING_FILE info {}", i);

        }

    }
}
