package org.talk.is.cheap.project.free.flow.starter.repository.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.rest_client.RestClientOptions;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.talk.is.cheap.project.free.flow.common.utils.PropertiesUtil;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Properties;

@AutoConfiguration(afterName = "org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration")
@Slf4j
public class EsAutoConfig {

    public static final String ES_CLIENT_BEAN_NAME = "repositoryStarterESClient";
    public static final String ASYNC_ES_CLIENT_BEAN_NAME = "repositoryStarterAsyncESClient";


    private volatile ElasticsearchTransport elasticsearchTransport;

    public ElasticsearchTransport elasticsearchTransport() throws IOException {

        if (elasticsearchTransport != null) {
            return elasticsearchTransport;
        }

        synchronized (this) {
            if (elasticsearchTransport != null) {
                return elasticsearchTransport;
            }
            Properties properties = PropertiesUtil.readFromFile("classpath:/es-config.properties");

            // 如果es有配置了密码，可以通过这种方式配置
            // 配置凭证提供器
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            // 设置用户名和密码
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getProperty("username"), properties.getProperty("password")));

            RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(properties.getProperty("host"),
                    Integer.parseInt(properties.getProperty("port")), "https"));
            // 连接延时配置
            restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> {
                requestConfigBuilder.setConnectTimeout(1000);
                requestConfigBuilder.setSocketTimeout(30000);
                requestConfigBuilder.setConnectionRequestTimeout(500);
                return requestConfigBuilder;
            });

            restClientBuilder.setHttpClientConfigCallback(httpAsyncClientBuilder ->
                    httpAsyncClientBuilder
                            .setMaxConnTotal(100)
                            .setMaxConnPerRoute(100)
                            .setSSLContext(buildSSLContext())
                            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                            .setDefaultCredentialsProvider(credentialsProvider));

            RestClient restClient = restClientBuilder.build();
            // Create the transport with a Jackson mapper


            // 配置驼峰与下划线转换
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy( PropertyNamingStrategies.SNAKE_CASE);

            elasticsearchTransport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
        }

        return elasticsearchTransport;
    }

    private SSLContext buildSSLContext() {

        // 读取http_ca.crt证书
        ClassPathResource resource = new ClassPathResource("git_ignore/http_ca.crt");
        SSLContext sslContext = null;
        try {
            // 证书工厂
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa;
            try (InputStream is = resource.getInputStream()) {
                trustedCa = factory.generateCertificate(is);
            }
            // 密钥库
            KeyStore trustStore = KeyStore.getInstance("pkcs12");
            trustStore.load(null, "domz".toCharArray());
            trustStore.setCertificateEntry("ca", trustedCa);

            SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                    .loadTrustMaterial(trustStore, null);
            sslContext = sslContextBuilder.build();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            log.error("ES连接认证失败", e);
        }
        return sslContext;
    }

    @Bean(ES_CLIENT_BEAN_NAME)
    public ElasticsearchClient elasticsearchClient() throws IOException {
        // And create the API client
        return new ElasticsearchClient(elasticsearchTransport());
    }

    @Bean(ASYNC_ES_CLIENT_BEAN_NAME)
    public ElasticsearchAsyncClient asyncClient() throws IOException {
        return new ElasticsearchAsyncClient(elasticsearchTransport());
    }


}
