package org.example.spring.starter.log.druid;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import org.example.spring.starter.log.Loggers;

/**
 * druid数据源的过滤器，用来打印sql的
 * @author dominiczhu
 * @version 1.0
 * @title Adapter
 * @date 2022/2/13 10:46 下午
 */
public class LogDruidFilterEventAdapter extends FilterEventAdapter {
    @Override
    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        statement.setLastExecuteStartNano();
    }

    @Override
    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {
        final String lastExecuteSql = statement.getLastExecuteSql().replaceAll("[\n\r]", " ").replaceAll(" +", " ");
        statement.setLastExecuteTimeNano();
        Loggers.SQL_LOG.info("{} - {}ms", lastExecuteSql, statement.getLastExecuteTimeNano() / 1000000);
    }
}
