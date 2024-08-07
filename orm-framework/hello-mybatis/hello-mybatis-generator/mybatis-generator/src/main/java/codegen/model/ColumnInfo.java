package codegen.model;

import codegen.model.enums.JavaType;
import com.mysql.cj.MysqlType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ColumnInfo {

    //数据库中列名
    private String columnName;
    //数据库中类型
    private MysqlType columnType;

    //DO对象中的对应的属性的名称
    private String propertyName;
    //DO对象中的对应的属性的java类型
    private JavaType propertyType;

    private boolean primaryKey;

    private boolean hidden;
}
