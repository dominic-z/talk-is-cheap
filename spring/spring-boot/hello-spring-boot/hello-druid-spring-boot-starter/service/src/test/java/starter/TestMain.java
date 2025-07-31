package starter;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.talk.is.cheap.hello.druid.spring.boot.starter.Main;
import org.talk.is.cheap.hello.druid.spring.boot.starter.service.DBService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SpringBootTest(classes = Main.class)
@Slf4j
public class TestMain {

    @Autowired
    DruidDataSource druidDataSource;

    @Autowired
    DBService dbService;


    @Test
    public void testDataSourceConfig(){
        log.info("getActiveCount: {}",druidDataSource.getPoolingCount());
    }

    @Test
    public void testCRUD() throws SQLException {
        log.info("druid datasource, {}", druidDataSource);

        try (PreparedStatement preparedStatement = druidDataSource.getPooledConnection().getConnection()
                .prepareStatement("select * from customers limit 5;");
        ) {
            // 传统的jdbc读取mysql数据方法
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                int customerNumber = resultSet.getInt("customerNumber");
                String customerName = resultSet.getString("customerName");
                log.info("customerNumber: {}, customerName: {}", customerNumber, customerName);
            }
        }



        Connection connection = druidDataSource.getPooledConnection().getConnection();
        connection.setAutoCommit(false);
        try (PreparedStatement preparedStatement = connection
                .prepareStatement("INSERT INTO `tokens` VALUES (?);");
        ) {
            // 传统的jdbc读取mysql数据方法
            preparedStatement.setString(1,"ttdd");
            boolean execute = preparedStatement.execute();
            connection.commit();

        }
    }


    @Test
    public void testTx() throws SQLException {
        dbService.simpleTx();
    }

}