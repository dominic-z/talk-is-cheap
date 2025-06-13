package resources.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SSLConfig {

    @Value("${server.port}")
    private int PORT;

    @Bean
    public ServletWebServerFactory servletContainer() {
        //        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        // 我也不知道为啥这么配，网上抄的
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };

        tomcat.addAdditionalTomcatConnectors(httpConnector());
        return tomcat;
    }

    @Bean
    public Connector httpConnector() {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        // http的默认端口，正常应该是80，http的默认端口，但是我电脑80端口是未开放的，如果改成80会有permission denied问题，所以先用8080代替一下。
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(PORT);
        return connector;
    }
}