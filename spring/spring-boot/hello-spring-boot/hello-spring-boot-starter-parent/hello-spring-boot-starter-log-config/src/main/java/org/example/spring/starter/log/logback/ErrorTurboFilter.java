package org.example.spring.starter.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.example.spring.starter.log.Loggers;
import org.slf4j.Marker;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ErrorTurboFilter
 * @date 2022/2/16 9:49 上午
 */
public class ErrorTurboFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (format == null) {
            return FilterReply.NEUTRAL;
        }

        // 把所有错误日志都汇总起来，不仅是业务打印的异常，还有框架本身的异常
        if (logger!=Loggers.ERROR_LOG && logger!=Loggers.ROOT_LOG && level == Level.ERROR) {
            Loggers.ERROR_LOG.error(format, params, t);
        }
        return FilterReply.NEUTRAL;
    }
}
