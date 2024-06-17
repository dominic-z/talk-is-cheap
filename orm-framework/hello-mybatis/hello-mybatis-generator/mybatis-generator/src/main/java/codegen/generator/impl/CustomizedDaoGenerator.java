package codegen.generator.impl;

import codegen.config.CodeGeneratorConfig;
import codegen.generator.CodeGenerator;
import codegen.model.TableInfo;
import codegen.util.FreemarkerUtil;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Dao层 代码生成
 */
@Slf4j
public class CustomizedDaoGenerator implements CodeGenerator {

    @Override
    public boolean genCode(TableInfo tableInfo) {
        Configuration cfg = FreemarkerUtil.getFreemarkerConfiguration();

        Map<String, Object> data = FreemarkerUtil.getDataMapInit(tableInfo);

        try {
            // 创建 Service 接口
            final Path servicePath = Paths.get(CodeGeneratorConfig.PROJECT_PATH, CodeGeneratorConfig.JAVA_PATH,
                    CodeGeneratorConfig.getInstance().getCustomizedDaoPackagePath(),
                    tableInfo.getDaoUpperCamelName() + ".java");

            // 查看父级目录是否存在, 不存在则创建
            if (!Files.exists(servicePath.getParent())) {
                Files.createDirectories(servicePath.getParent());
            }
            cfg.getTemplate("dao.ftl").process(data, new FileWriter(servicePath.toFile()));

            log.info("{}Dao.java 生成成功!", tableInfo.getDomainUpperCamelName());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Dao 生成失败!", e);
        }
    }

}
