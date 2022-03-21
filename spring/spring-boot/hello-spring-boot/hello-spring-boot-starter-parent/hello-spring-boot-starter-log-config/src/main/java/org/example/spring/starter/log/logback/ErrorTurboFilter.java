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
        // 如果当前error日志没有走ERROR_LOG
        if (!logger.getName().equals("error") && !logger.getName().equals("root") && level == Level.ERROR) {
            Loggers.ERROR_LOG.error(format, params, t);
        }
        return FilterReply.NEUTRAL;
    }
}
