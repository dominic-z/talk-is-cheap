package codegen.model;

import codegen.util.StringUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author dominiczhu
 * @version 1.0
 * @title TableInfo
 * @date 2022/1/13 4:09 下午
 */
@Data
public class TableInfo {

    public static final Set<String> IGNORE_COLUMN_FOR_ALL = new HashSet<>(Arrays.asList("deleted"));

    public static final Set<String> IGNORE_COLUMN_FOR_DOMAIN = new HashSet<>(
            Arrays.asList("id", "revision", "deleted", "createdTime", "lastModifiedTime"));

    private String tableName;

    private String mbgExampleUpperCamelName;

    private String mbgExampleLowerCamelName;

    private String domainUpperCamelName;

    private String domainLowerCamelName;

    private String daoUpperCamelName;

    private String mapperUpperCamelName;

    private String mapperLowerCamelName;

    private String daoLowerCamelName;

    private String queryUpperCamelName;

    private String queryLowerCamelName;

    private String serviceUpperCamelName;

    private String serviceLowerCamelName;

    /**
     * all columns of current generated table.
     */
    private List<String> insertColumnsList;

    private List<String> insertPropList;

    private List<String> columnAndPropList;

    private List<ColumnInfo> columnInfoList;

    private List<ColumnInfo> updateColumnInfoList;

    private List<ColumnInfo> domainColumnInfoList;

    private ColumnInfo pkColumn;


    public TableInfo(String tableName) {
        this.tableName = tableName;
        this.domainUpperCamelName = StringUtil.tableNameConvertUpperCamel(tableName);
        this.domainLowerCamelName = StringUtil.tableNameConvertLowerCamel(tableName);

        this.mbgExampleUpperCamelName = domainUpperCamelName + "Example";
        this.mbgExampleLowerCamelName = domainLowerCamelName + "Example";

        this.mapperUpperCamelName = domainUpperCamelName + "Mapper";
        this.mapperLowerCamelName = domainLowerCamelName + "Mapper";

        this.daoUpperCamelName = domainUpperCamelName + "Dao";
        this.daoLowerCamelName = domainLowerCamelName + "Dao";

        this.serviceUpperCamelName = domainUpperCamelName + "Service";
        this.serviceLowerCamelName = domainLowerCamelName + "Service";

        this.insertColumnsList = new ArrayList<>();
        this.insertPropList = new ArrayList<>();
        this.columnAndPropList = new ArrayList<>();
        this.columnInfoList = new ArrayList<>();
        this.updateColumnInfoList = new ArrayList<>();
        this.domainColumnInfoList = new ArrayList<>();
    }

}
