package codegen.util;


import codegen.model.ColumnInfo;
import codegen.model.enums.JavaType;
import com.mysql.cj.MysqlType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.*;
import java.util.*;

/**
 * 获取数据库元信息
 */
@Slf4j
public class DbInfoUtil {

    /**
     * code cc
     *
     * @param driver
     * @param url
     * @param user
     * @param pwd
     * @param table
     * @param database
     * @return
     */
    public static Map<String, ColumnInfo> getTableInfo(String driver, String url, String user, String pwd, String table,
                                                       String database) throws Exception {
//        List<ColumnInfo> result = new ArrayList<>();
        Map<String, ColumnInfo> result = new HashMap<>();

        Connection conn = null;
        DatabaseMetaData dbmd = null;

        try {
            conn = getConnections(driver, url, user, pwd);

            dbmd = conn.getMetaData();

            String primaryKeyColumnName = "";
            ResultSet primaryKeyResultSet = dbmd.getPrimaryKeys(database, null, table);
            while (primaryKeyResultSet.next()) {
                primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
            }

            ResultSet resultSet = dbmd.getTables(database, "%", table, new String[]{"TABLE"});
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");

                if (tableName.equals(table)) {
                    ResultSet rs = conn.getMetaData()
                            .getColumns(database, "%", tableName, "%"); // % 通配符

                    while (rs.next()) {
                        ColumnInfo columnInfo = new ColumnInfo();

                        String colName = rs.getString("COLUMN_NAME");
                        columnInfo.setColumnName(colName);

                        MysqlType columnType = MysqlType.valueOf(rs.getString("TYPE_NAME"));

                        columnInfo.setColumnType(columnType);

                        String propertyName = StringUtil.underScoreToLowerCamel(colName);
                        columnInfo.setPropertyName(propertyName);


                        JavaType propertyType = mysqlTypeToJavaType(columnType);
                        if (propertyType == null) {
                            log.error("表{}列{}类型转换异常，无法转换的类型{}", table, colName, columnType);
                        }
                        columnInfo.setPropertyType(propertyType);

                        if (colName.equalsIgnoreCase(primaryKeyColumnName)) {
                            columnInfo.setPrimaryKey(true);
                        }

                        result.put(colName, columnInfo);
                    }
                }
            }

        } catch (Exception e) {

            log.info("{} 读取表数据失败", table, e);
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static String changeColumnName(String colName) {

        return colName;
    }


    private static JavaType mysqlTypeToJavaType(MysqlType mysqlType) {

        switch (mysqlType) {
            case VARCHAR:
            case CHAR:
                return JavaType.StringType;
            case DECIMAL:
                return JavaType.BigDecimalType;
            case BIT:
            case INT:
            case INT_UNSIGNED:
            case SMALLINT:
            case TINYINT:
                return JavaType.IntegerType;
            case BIGINT:
                return JavaType.LongType;
            case DATETIME:
            case TIMESTAMP:
            case DATE:
                return JavaType.DateType;
            case BLOB:
                return JavaType.ByteArrayType;
            case FLOAT:
                return JavaType.FloatType;
            case DOUBLE:
                return JavaType.DoubleType;
            default:
                return null;
        }
    }

    private static String DBTypeToJavaType(String dbType) {
        dbType = dbType.toUpperCase();
        switch (dbType) {
            case "VARCHAR":
            case "CHAR":
                return "String";
            case "NUMBER":
            case "DECIMAL":
                return "java.math.BigDecimal";
            case "BIT":
            case "INT":
            case "INT UNSIGNED":
            case "SMALLINT":
            case "TINYINT":
            case "INTEGER":
                return "Integer";
            case "BIGINT":
                return "Long";
            case "DATETIME":
            case "TIMESTAMP":
            case "DATE":
                return "Date";
            case "BLOB":
                return "Byte[]";
            case "FLOAT":
                return "Float";
            case "DOUBLE":
                return "Double";
            default:
                return "String";
        }
    }

    //获取连接
    private static Connection getConnections(String driver, String url, String user, String pwd) throws Exception {
        Connection conn = null;
        try {
            Properties props = new Properties();
            props.put("remarksReporting", "true");
            props.put("user", user);
            props.put("password", pwd);
            val aClass = Class.forName(driver);
            conn = DriverManager.getConnection(url, props);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return conn;
    }

    private static String getSchema(Connection conn) throws Exception {
        String schema;
        schema = conn.getMetaData().getUserName();
        if ((schema == null) || (schema.length() == 0)) {
            throw new Exception("数据库模式不允许为空");
        }
        return schema.toUpperCase().toString();

    }

//    public static void main(String[] args) {
//        //mysql
//        String driver = "com.mysql.jdbc.Driver";
//        String user = "dev";
//        String pwd = "dev";
//        String url = "jdbc:mysql://9.134.12.243:3306/test";
//        String table = "t_user";
//
//        List list = getTableInfo(driver, url, user, pwd, table);
//        System.out.println(list);
//    }
}
