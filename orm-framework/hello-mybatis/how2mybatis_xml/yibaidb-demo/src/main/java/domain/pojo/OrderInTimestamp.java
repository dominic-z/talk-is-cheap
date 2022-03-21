package domain.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @author dominiczhu
 * @version 1.0
 * @title OrderInTimestamp
 * @date 2021/8/31 上午10:47
 */
@Data
public class OrderInTimestamp {

    private int orderNumber;

    private Date orderDate;

    private Date requiredDate;

    private Date shippedDate;

    private String status;

    private String comments;

    private int customerNumber;

}
