package geektime.spring.data;

import geektime.spring.data.domain.pojo.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Application
 * @date 2021/9/14 上午11:12
 */

@SpringBootApplication
@EnableConfigurationProperties
@Slf4j
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    @Qualifier("yiibaiJdbcTemplate")
    private JdbcTemplate yiibaiJdbcTemplate;

    @Autowired
    @Qualifier("dmpJdbcTemplateTwo")
    private JdbcTemplate dmpJdbcTemplateTwo;

    @Override
    public void run(String... args) throws Exception {
        List<Item> items=yiibaiJdbcTemplate.query("select * from items", new RowMapper<Item>() {
            @Override
            public Item mapRow(ResultSet resultSet, int i) throws SQLException {
                int id=resultSet.getInt(1);
                String itemNo = resultSet.getString(2);
                Item item=new Item();
                item.setId(id);
                item.setItemNo(itemNo);
                return item;
            }
        });

        items.stream().limit(20).forEach(item -> log.info(item.toString()));

        log.info("分割行");

        items=yiibaiJdbcTemplate.query("select * from items",new BeanPropertyRowMapper<>(Item.class));
        items.stream().limit(20).forEach(item -> log.info(item.toString()));


    }
}
