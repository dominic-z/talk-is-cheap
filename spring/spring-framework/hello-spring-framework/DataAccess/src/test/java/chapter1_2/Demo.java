package chapter1_2;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Demo {
    @Test
    public void test()  {
        ApplicationContext ac=new ClassPathXmlApplicationContext("chapter1_2/context.xml");
        DataSource dataSource = ac.getBean("dataSource", DataSource.class);
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement preparedStatement = conn.prepareStatement("select * from author");
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            while(resultSet.next()){
                String string = resultSet.getString(2);
                System.out.println(string);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
