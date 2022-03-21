package test;

import config.SqlSessionConfig;
import domain.pojo.OrderInTimestamp;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title OdersInTimestamp
 * @date 2021/8/31 上午10:48
 */
public class OdersInTimestamp {


    @Test
    public void testTimestamp() throws IOException, ParseException {
        SqlSession sqlSession = SqlSessionConfig.getSqlSession();

        HashMap<String, Object> params = new HashMap<>();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date startDate = simpleDateFormat.parse("20130128");
        Date endDate = simpleDateFormat.parse("20130328");
        params.put("startDate",startDate);
        params.put("endDate",endDate);

        List<OrderInTimestamp> orderInTimestamps = sqlSession.selectList("orders_in_timestamp.rangeSelectByOrderDate",params);
        System.out.println(orderInTimestamps);
    }

}
