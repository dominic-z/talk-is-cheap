package geektime.spring.data.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author dominiczhu
 * @version 1.0
 * @title jdbctemplate
 * @date 2021/9/14 上午11:04
 */
@Configuration
public class JdbcTemplateConfig {

    @Bean
    JdbcTemplate yiibaiJdbcTemplate(@Qualifier("yiibaiDatasource") DataSource yiibaiDataSource) {
        return new JdbcTemplate(yiibaiDataSource);
    }

    @Bean
    JdbcTemplate dmpJdbcTemplateTwo(@Qualifier("dmpDatasource") DataSource dmpDatasource) {
        return new JdbcTemplate(dmpDatasource);
    }

}
