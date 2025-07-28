package org.talk.is.cheap.project.free.common.utils;

import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class YamlUtils {

    private YamlUtils() {
    }

    public static <T> T load(URL file, Class<T> tClass) {
        // yaml默认不认识短横线命名法，得手动折腾
        // https://www.doubao.com/thread/w778ef7a571b18254 加上yaml默认构造方法的源码
        try (InputStream inputStream = new FileInputStream(file.getPath())) {
            Constructor constructor = new Constructor(tClass, new LoaderOptions());
            constructor.setPropertyUtils(new PropertyUtils() {
                @Override
                public Property getProperty(Class<?> type, String name) {
                    return super.getProperty(type, CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name));
                }
            });
            Yaml yaml = new Yaml(constructor);
            T obj = yaml.loadAs(inputStream, tClass);
            log.info("解析 {} 对象成功: {}", tClass, obj);
            return obj;
        } catch (IOException e) {
            log.error("解析 {} 对象失败", tClass, e);
            return null;
        }
    }
}
