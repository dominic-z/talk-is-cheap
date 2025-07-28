package hello.spring.boot3.spi.starter.starter.config;

import hello.spring.boot3.spi.starter.starter.service.impl.KonnichiwaServiceImpl;
import org.springframework.context.annotation.Import;


@Import({KonnichiwaServiceImpl.class})
public class AnnotationStarterConfiguration {

}
