package codegen;

import codegen.config.CodeGeneratorConfig;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.JDBCType;
import java.util.Map;

@Slf4j
public class UnitTest {
    @Getter
    public static class TestConfig {
        private int v1;

        private int v2;

        @Data
        public static class P {
            private String v1;
            private int v2;
        }

        private P p1;
    }

    @Test
    public void readYamlTest() throws IOException {

        InputStream inputStream = UnitTest.class.getClassLoader().getResourceAsStream("test-config.yml");
        inputStream.mark(Integer.MAX_VALUE);
        Object rawYaml = new Yaml().load(inputStream);
        log.info("raw yaml, {}", rawYaml);
        inputStream.reset();
        TestConfig config = new Yaml().loadAs(inputStream, TestConfig.class);
        log.info("class yaml, {}", config);


    }


    @Test
    public void testReadConfig() {
//        CodeGeneratorConfig.setConfigPath("src/main/resources/config.yml");
//        log.info("{}", CodeGeneratorConfig.getInstance());

        log.info("{}",JDBCType.valueOf("INTEGER"));
    }

}
