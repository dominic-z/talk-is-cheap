package codegen.util;

import codegen.CodeGeneratorManager;
import codegen.config.CodeGeneratorConfig;
import codegen.model.TableInfo;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.val;

import java.util.HashMap;
import java.util.Map;


public class FreemarkerUtil {

    private static Configuration configuration;

    /**
     * 预置页面所需数据
     *
     * @param tableInfo 表信息
     * @return
     */
    public static Map<String, Object> getDataMapInit(TableInfo tableInfo) {
        Map<String, Object> data = new HashMap<>();

        Map<String, String> envProps = System.getenv();
        String userName = envProps.get("USERNAME");// 获取用户名
        if (StringUtil.isEmpty(userName)) {
            userName = "codegen";
        }

        data.put("date", CodeGeneratorConfig.DATE);
        data.put("author", userName);
        data.put("tableName", tableInfo.getTableName());
        data.put("serviceUpperCamelName", tableInfo.getServiceUpperCamelName());
        data.put("serviceLowerCamelName", tableInfo.getServiceLowerCamelName());

        data.put("daoUpperCamelName", tableInfo.getDaoUpperCamelName());
        data.put("daoLowerCamelName", tableInfo.getDaoLowerCamelName());

        data.put("mapperUpperCamelName", tableInfo.getMapperUpperCamelName());
        data.put("mapperLowerCamelName", tableInfo.getMapperLowerCamelName());

        data.put("domainLowerCamelName", tableInfo.getDomainLowerCamelName());
        data.put("domainUpperCamelName", tableInfo.getDomainUpperCamelName());

        data.put("exampleUpperCamelName", tableInfo.getMbgExampleUpperCamelName());
        data.put("exampleLowerCamelName", tableInfo.getMbgExampleLowerCamelName());

        val config = CodeGeneratorConfig.getInstance();
        data.put("basePackage", config.getBasePackage());
        data.put("customizedDaoPackage", config.getCustomizedDaoPackage());
        data.put("mbgMapperPackage", config.getMBGMapperPackage());
        data.put("servicePackage", config.getServicePackage());

        data.put("modelPackage", config.getModelPackage());
        data.put("mgbExamplePackage", config.getMbgExamplePackage());
        return data;
    }


    /**
     * 获取 Freemarker 模板环境配置
     *
     * @return
     */
    public static Configuration getFreemarkerConfiguration() {
        if (configuration == null) {
            configuration = initFreemarkerConfiguration();
        }
        return configuration;
    }

    /**
     * Freemarker 模板环境配置
     *
     * @return
     */
    private static Configuration initFreemarkerConfiguration() {
        Configuration cfg = null;
        try {
            cfg = new Configuration(Configuration.VERSION_2_3_23);
            //cfg.setDirectoryForTemplateLoading(new File(CodeGeneratorConfig.TEMPLATE_FILE_PATH));
            cfg.setClassLoaderForTemplateLoading(CodeGeneratorManager.class.getClassLoader(), "/template");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        } catch (Exception e) {
            throw new RuntimeException("Freemarker 模板环境初始化异常!", e);
        }
        return cfg;
    }
}
