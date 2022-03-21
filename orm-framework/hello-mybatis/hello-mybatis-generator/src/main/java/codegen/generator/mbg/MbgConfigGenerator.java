package codegen.generator.mbg;

import codegen.config.CodeGeneratorConfig;
import codegen.model.TableInfo;
import codegen.util.FreemarkerUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MbgConfigGenerator
 * @date 2022/1/11 9:17 下午
 */
@Slf4j
public class MbgConfigGenerator {


    /**
     * 生成mbg配置的输入流
     *
     * @param tableName
     * @return
     * @throws Exception
     */
    public static InputStream getMbgConfig(TableInfo tableInfo) throws Exception {

        final Configuration freemarkerConfig = FreemarkerUtil.getFreemarkerConfiguration();


        final Map<String, Object> freemarkerData = FreemarkerUtil.getDataMapInit(tableInfo);

        freemarkerData.put("JDBC_DRIVER", CodeGeneratorConfig.JDBC_DRIVER_CLASS_NAME);
        freemarkerData.put("JDBC_URL", CodeGeneratorConfig.JDBC_URL);
        freemarkerData.put("JDBC_USERNAME", CodeGeneratorConfig.JDBC_USERNAME);
        freemarkerData.put("JDBC_PASSWORD", CodeGeneratorConfig.JDBC_PASSWORD);
        freemarkerData.put("JAVA_PATH", CodeGeneratorConfig.JAVA_PATH);
        freemarkerData.put("RESOURCES_PATH", CodeGeneratorConfig.RESOURCES_PATH);
        freemarkerData.put("SQL_MAPPER_XML_ROOT_PATH", CodeGeneratorConfig.SQL_MAPPER_XML_ROOT_PATH);

        Path javaPath = Paths.get(CodeGeneratorConfig.JAVA_PATH);
        if (!Files.exists(javaPath)) {
            Files.createDirectories(javaPath);
        }
        Path resourcePath = Paths.get(CodeGeneratorConfig.RESOURCES_PATH);
        if (!Files.exists(resourcePath)) {
            Files.createDirectories(resourcePath);
        }

        try {
            StringWriter tempOutput = new StringWriter();
            freemarkerConfig.getTemplate("mybatis-gen-config.ftl").process(freemarkerData, tempOutput);
            return new ByteArrayInputStream(tempOutput.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.info("生成mgb配置文件失败");
            throw e;
        }
    }
}
