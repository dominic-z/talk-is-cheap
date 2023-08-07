package codegen;

import codegen.config.CodeGeneratorConfig;
import codegen.generator.impl.CustomizedDaoGenerator;
import codegen.generator.impl.ServiceGenerator;
import codegen.generator.mbg.MbgCodeGenerator;
import codegen.model.ColumnInfo;
import codegen.model.TableInfo;
import codegen.util.DbInfoUtil;
import codegen.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * 代码生成器基础项
 */
@Slf4j
public class CodeGeneratorManager {


    static {
        // 初始化配置信息
        init(null);
    }


    /**
     * 生成代码
     */
    public void genCode() throws Exception {
        if (CodeGeneratorConfig.GENERATE_TABLE == null) {
            throw new IllegalArgumentException("table is null");
        }

        //删除目录
        try {
            FileUtils.deleteDirectory(new File(CodeGeneratorConfig.JAVA_PATH));
            FileUtils.deleteDirectory(new File(CodeGeneratorConfig.RESOURCES_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] tableNames = CodeGeneratorConfig.GENERATE_TABLE.split(",");

        for (String tableName : tableNames) {
            genCodeByTableName(tableName);
        }

        log.info("output dir: {}",
                CodeGeneratorConfig.PROJECT_PATH + File.separator + "target" + File.separator + "out");
    }

    /**
     * 通过数据库表名生成代码
     *
     * @param tableName 表名
     */
    private void genCodeByTableName(String tableName) throws Exception {
        final TableInfo tableInfo = getTableInfo(tableName);

        new MbgCodeGenerator().genCode(tableInfo);
        new CustomizedDaoGenerator().genCode(tableInfo);
        new ServiceGenerator().genCode(tableInfo);

    }

    private TableInfo getTableInfo(String tableName) throws Exception {
        TableInfo tableInfo = new TableInfo(tableName);

        String[] splits = CodeGeneratorConfig.JDBC_URL.split("/");
        String database = splits[splits.length - 1];
        List<ColumnInfo> columnInfoList = DbInfoUtil.getTableInfo(CodeGeneratorConfig.JDBC_DRIVER_CLASS_NAME,
                CodeGeneratorConfig.JDBC_URL,
                CodeGeneratorConfig.JDBC_USERNAME,
                CodeGeneratorConfig.JDBC_PASSWORD, tableName, database);

        tableInfo.setColumnInfoList(columnInfoList);

        for (ColumnInfo columnInfo : columnInfoList) {
            if (TableInfo.IGNORE_COLUMN_FOR_ALL.contains(columnInfo.getColumnName())) {
                continue;
            }

//            if (!columnInfo.isPrimaryKey()) {
//                tableInfo.getInsertColumnsList().add(columnInfo.getColumnName());
//                tableInfo.getInsertPropList().add("#{" + columnInfo.getPropertyName() + "}");
//
//                tableInfo.getUpdateColumnInfoList().add(columnInfo);
//            }
//
//            if (columnInfo.getColumnName().contains("_")) {
//                tableInfo.getColumnAndPropList().add(columnInfo.getColumnName() + " " + columnInfo.getPropertyName());
//            } else {
//                tableInfo.getColumnAndPropList().add(columnInfo.getColumnName());
//            }
//
//            if (!TableInfo.IGNORE_COLUMN_FOR_DOMAIN.contains(columnInfo.getPropertyName())) {
//                tableInfo.getDomainColumnInfoList().add(columnInfo);
//            }

        }

        return tableInfo;
    }

    /**
     * 初始化配置信息
     */
    private static void init(Properties userProp) {
        Properties prop = loadProperties();

        if (userProp != null) {
            prop.putAll(userProp);
        }

        CodeGeneratorConfig.JDBC_URL = prop.getProperty("jdbc.url");
        CodeGeneratorConfig.JDBC_USERNAME = prop.getProperty("jdbc.username");
        CodeGeneratorConfig.JDBC_PASSWORD = prop.getProperty("jdbc.password");

        CodeGeneratorConfig.GENERATE_TABLE = prop.getProperty("generate.table");

        String basePackage = prop.getProperty("base.package");
        CodeGeneratorConfig.BASE_PACKAGE = basePackage;
        CodeGeneratorConfig.MODEL_PACKAGE = basePackage + ".domain.pojo";
        CodeGeneratorConfig.MBG_EXAMPLE_PACKAGE = basePackage + ".domain.query.example";
        CodeGeneratorConfig.MBG_MAPPER_PACKAGE = basePackage + ".dao.mbg";
        CodeGeneratorConfig.CUSTOMIZED_DAO_PACKAGE = basePackage + ".dao.customized";
        CodeGeneratorConfig.SERVICE_PACKAGE = basePackage + ".service";

        CodeGeneratorConfig.CUSTOMIZED_DAO_PACKAGE_PATH =
                StringUtil.packageConvertPath(CodeGeneratorConfig.CUSTOMIZED_DAO_PACKAGE);
        CodeGeneratorConfig.SERVICE_PACKAGE_PATH = StringUtil.packageConvertPath(CodeGeneratorConfig.SERVICE_PACKAGE);

        CodeGeneratorConfig.DATE = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        CodeGeneratorConfig.INITIALIZED = true;
    }

    /**
     * 加载配置文件
     *
     * @return
     */
    private static Properties loadProperties() {
        Properties prop = null;
        try {
            prop = new Properties();
            InputStream in = CodeGeneratorManager.class.getClassLoader().getResourceAsStream("config.properties");
            prop.load(in);
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件异常!", e);
        }
        return prop;
    }

}
