import org.junit.Test;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class TimeZoneTest {

    Connection conn;

    private Connection getConnection() {

        if (conn != null) {
            return conn;
        }
//        final String URL = "jdbc:mysql://localhost:3306/tz_test_db?serverTimezone=Asia/Shanghai";
        final String URL = "jdbc:mysql://localhost:3306/tz_test_db?serverTimezone=UTC";
        final String USER = "root";
        final String PASSWORD = "Test@2026";

        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return conn;
    }


    @Test
    public void insertTimeZone() throws SQLException {
        Connection connection = getConnection();
        String sql = "insert into tz_test_db.tz_test (ts_time,dt_time) values (?,?);";
        DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.of("UTC"));
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println(TimeZone.getDefault().getID());
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
//            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
//            LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
//            System.out.println(FMT.format(now));
//            ps.setObject(1,now);
//            ps.setObject(2,now);

//            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
//            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            ps.setObject(1,new Date());
            ps.setObject(2,new Date());
            ps.executeUpdate();
        }
    }

    @Test
    public void readTimeZone() throws SQLException {

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.println(TimeZone.getDefault().getID());
        Statement stmt = getConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM tz_test_db.tz_test");
        //如果有数据，rs.next()返回true
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//                .withZone(ZoneId.of("UTC"))
                .withZone(ZoneId.of("Asia/Shanghai"))
                ;
        while (rs.next()) {
//            LocalDateTime tsTime = rs.getObject("ts_time", LocalDateTime.class);
//            LocalDateTime dtTime = rs.getObject("dt_time", LocalDateTime.class);
//            System.out.printf("ts时间：%s, dt时间:%s %n",
//                    FMT.format(tsTime),
//                    FMT.format(dtTime));


            System.out.printf("ts时间：%s, dt时间:%s %n",
                    rs.getTimestamp("ts_time"),
                    rs.getTimestamp("dt_time"));
        }

        conn.close();
    }

}
