package codegen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 测试mgb的demo
 * @author dominiczhu
 * @version 1.0
 * @title MgbDemo
 * @date 2022/1/11 9:00 下午
 */
@Slf4j
public class MgbDemo {

    public static void main(String[] args) throws Exception {

//        new MbgCodeGenerator().genCode("auto_dashboard");

        useMGB();
    }

    private static void useMGB() throws Exception {
        final Path outPath = Paths.get(System.getProperty("user.dir"), "mybatis-generator","target", "out");
        log.info("{}",outPath);
        if (Files.exists(outPath)) {
            FileUtils.deleteDirectory(outPath.toFile());
        }
        Files.createDirectories(outPath);

        List<String> warnings = new ArrayList<String>();
        boolean overwrite = true;
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(
                CodeGeneratorMain.class.getResourceAsStream("/mybatis-gen-config.xml"));
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);

    }

}
