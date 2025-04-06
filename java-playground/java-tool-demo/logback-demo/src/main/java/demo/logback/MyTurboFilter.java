package demo.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import demo.logback.util.Loggers;
import org.slf4j.Marker;

import java.util.Arrays;


/**
 * Turbo Filter 是 Logback 中一种特殊的过滤器，它可以在日志事件被记录到输出目的地（如控制台、文件等）之前，对日志事件进行快速筛选和处理。与普通的过滤器相比，Turbo Filter
 * 的执行优先级更高，能够更高效地决定是否要记录某个日志事件，常用于实现一些全局的、对性能要求较高的日志过滤逻辑。
 *
 * 返回值为ALLOWED（允许）、DENIED（拒绝）或NEUTRAL（交由后续过滤器或规则处理）。
 *
 */
public class MyTurboFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable) {
        System.out.println(marker);
        System.out.println(logger);
        System.out.println(level);
        System.out.println(s);
        System.out.println(Arrays.toString(objects));
        System.out.println(throwable);
        return FilterReply.NEUTRAL;
    }
}
