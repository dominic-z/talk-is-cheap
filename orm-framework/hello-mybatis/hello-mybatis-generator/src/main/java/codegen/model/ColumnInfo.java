package codegen.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ColumnInfo {

    private String columnName;
    private String columnType;

    private String propertyName;
    private String propertyType;

    private boolean primaryKey;

    private boolean hidden;
}
