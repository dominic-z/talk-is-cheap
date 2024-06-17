package codegen.config;

import codegen.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 配置信息变量
 */
@Slf4j
@Data
public class CodeGeneratorConfig {

    public static boolean INITIALIZED = false;

    // 项目在硬盘上的基础路径
    public static final String PROJECT_PATH = System.getProperty("user.dir");
    // java文件路径
    public static final String JAVA_PATH = "mybatis-generator/target/out/src/main/java";
    // 资源文件路径
    public static final String RESOURCES_PATH = "mybatis-generator/target/out/src/main/resources";
    public static final String SQL_MAPPER_XML_ROOT_PATH = RESOURCES_PATH + "/mappers";
//    // JDBC 相关配置信息
//    public static String JDBC_URL;
//    public static String JDBC_USERNAME;
//    public static String JDBC_PASSWORD;
//    public static String JDBC_DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
//
//    public static String GENERATE_TABLE;
//
//
//    // 包的父目录
//    public static String BASE_PACKAGE;
//    // 项目 Model 所在包
//    public static String MODEL_PACKAGE;
//    // mbg生成的example路径
//    public static String MBG_EXAMPLE_PACKAGE;
//    // 项目 Service 所在包
//    public static String MBG_MAPPER_PACKAGE;
//
//    public static String CUSTOMIZED_DAO_PACKAGE;
//    // 项目 Service 所在包
//    public static String SERVICE_PACKAGE;
//
//
//    // 生成的 Dao 存放路径
//    public static String CUSTOMIZED_DAO_PACKAGE_PATH;
//    // 生成的 Service 存放路径
//    public static String SERVICE_PACKAGE_PATH;

    // 模板注释中 @author
    public static String AUTHOR = "";
    // 模板注释中 @date
    public static final String DATE = new SimpleDateFormat("yyyy/MM/dd").format(new Date());


    @Data
    public static class Datasource {
        private String driver;
        private String url;
        private String username;
        private String password;

    }

    @Data
    public static class TableConfig {

        @Data
        public static class ColumnConfig {
            private int id;
            private String name;
            private List<EnumeratedConstant> enumeratedConstants;

            @Data
            public static class EnumeratedConstant {
                private int id;
                private String literal;
                private String value;
                private String description;
            }
        }

        private int id;
        private String name;
        private List<ColumnConfig> columns;
    }

    private CodeGeneratorConfig() {
    }

    private static final Object LOCK = new Object();
    private static volatile CodeGeneratorConfig instance;

    private static String CONFIG_PATH = "config.yml";

    private Datasource datasource;
    private List<TableConfig> tables;

    private Map<String, TableConfig> tableConfigMap;
    private String basePackage;


    public static void setConfigPath(String path) {
        CONFIG_PATH = path;
    }

    public static CodeGeneratorConfig getInstance() {
        if (instance == null) {
            synchronized (CodeGeneratorConfig.class) {
                if (instance == null) {
                    instance = loadYaml();
                }
                if (instance.getTables() == null) {
                    throw new IllegalArgumentException("config error: table is null or empty");
                }

                val datasource1 = instance.getDatasource();
                if (datasource1 == null || datasource1.getUrl() == null || datasource1.getUsername() == null || datasource1.getPassword() == null || datasource1.getDriver() == null) {
                    throw new IllegalArgumentException("config error: datasource config is null or empty");
                }

                instance.tableConfigMap = instance.getTables().stream().collect(Collectors.toMap(TableConfig::getName
                        , t -> t));
            }
        }

        return instance;
    }


    private static CodeGeneratorConfig loadYaml() {
        InputStream inputStream = CodeGeneratorConfig.class.getClassLoader()
                .getResourceAsStream(CodeGeneratorConfig.CONFIG_PATH);
        if (inputStream == null) {
            throw new RuntimeException("配置文件缺失" + CodeGeneratorConfig.CONFIG_PATH);
        }

        CodeGeneratorConfig config = new Yaml().loadAs(inputStream, CodeGeneratorConfig.class);

        return config;
    }

    public String getModelPackage() {
        return basePackage + ".domain.pojo";
    }

    public String getMbgExamplePackage() {
        return basePackage + ".domain.query.example";
    }

    public String getMBGMapperPackage() {
        return basePackage + ".dao.mbg";
    }

    public String getCustomizedDaoPackage() {
        return basePackage + ".dao.customized";
    }

    public String getServicePackage() {
        return basePackage + ".service";
    }

    public String getCustomizedDaoPackagePath() {
        return StringUtil.packageConvertPath(getCustomizedDaoPackage());
    }

    public String getServicePackagePath() {
        return StringUtil.packageConvertPath(getServicePackage());
    }
}
