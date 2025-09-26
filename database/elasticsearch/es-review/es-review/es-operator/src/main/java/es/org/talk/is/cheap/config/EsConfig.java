package es.org.talk.is.cheap.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

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

@Configuration
@Slf4j
public class EsConfig {

    @Value("${es.host}")
    private String esHost;
    @Value("${es.port}")
    private int esPort;

    @Value("${es.username}")
    private String username;

    @Value("${es.password}")
    private String password;
    @Bean
    public ElasticsearchTransport restClientTransport() {

        // 如果es有配置了密码，可以通过这种方式配置
        // 配置凭证提供器
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // 设置用户名和密码
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        var restClient = RestClient.builder(new HttpHost(esHost, esPort,"https"))
                .setHttpClientConfigCallback(httpAsyncClientBuilder ->  httpAsyncClientBuilder
                        .setMaxConnTotal(100)
                        .setMaxConnPerRoute(100)
                        .setSSLContext(buildSSLContext())
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();
//        如果不需要https访问，那么这样就行
//        var restClient = RestClient.builder(new HttpHost(esHost, esPort))
//                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
//                .build();
        // Create the transport with a Jackson mapper
        return new RestClientTransport(
                restClient, new JacksonJsonpMapper());
    }
    @Bean("esClient")
    public ElasticsearchClient elasticsearchClient(
            @Autowired ElasticsearchTransport restClientTransport){
        // And create the API client
        return new ElasticsearchClient(restClientTransport);
    }

    @Bean
    public ElasticsearchAsyncClient asyncClient(@Autowired ElasticsearchTransport restClientTransport){
        return new ElasticsearchAsyncClient(restClientTransport);
    }


    /**
     * 配置ssl访问，如果不用https访问就注释掉
     * @return
     */
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
}
