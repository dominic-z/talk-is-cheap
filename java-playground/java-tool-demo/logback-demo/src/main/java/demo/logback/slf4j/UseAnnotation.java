package demo.logback.slf4j;

import lombok.extern.slf4j.Slf4j;

/**
 * @author dominiczhu
 * @version 1.0
 * @title UseAnnotation
 * @date 2022/2/13 3:07 下午
 */
@Slf4j(topic = "file")
public class UseAnnotation {
    public static void main(String[] args) {
        log.info("slf4j file info");
    }
}
