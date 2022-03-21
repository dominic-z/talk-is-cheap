package codegen.generator.mbg;

import codegen.generator.CodeGenerator;
import codegen.model.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title MbgConfigGenerator
 * @date 2022/1/11 9:17 下午
 */
@Slf4j
public class MbgCodeGenerator implements CodeGenerator {

//    protected static final Logger logger = LogManager.getLogger(MbgCodeGenerator.class);

    public boolean genCode(TableInfo tableInfo) {

        try {
            final InputStream mgbConfig = MbgConfigGenerator.getMbgConfig(tableInfo);

            List<String> warnings = new ArrayList<String>();
            ConfigurationParser cp = new ConfigurationParser(warnings);
            Configuration config = cp.parseConfiguration(mgbConfig);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);

            myBatisGenerator.generate(null);

//            logger.info("{}的mgb生成成功!", tableName);
            return true;
        } catch (Exception e) {
//            logger.error("{}的mgb执行失败，可能导致丢失domain类.xml，退化为ftl生成的domain!", tableName, e);

            return false;
        }

    }
}
