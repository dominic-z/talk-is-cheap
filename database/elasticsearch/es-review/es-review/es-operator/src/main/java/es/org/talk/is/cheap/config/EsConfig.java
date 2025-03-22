package es.org.talk.is.cheap.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsConfig {

    @Value("${es.host}")
    private String esHost;
    @Value("${es.port}")
    private int esPort;
    @Bean
    public ElasticsearchTransport restClientTransport() {

        // 如果es有配置了密码，可以通过这种方式配置
//        // 配置凭证提供器
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        // 设置用户名和密码
//        credentialsProvider.setCredentials(AuthScope.ANY,
//                new UsernamePasswordCredentials("your_username", "your_password"));

        var restClient = RestClient.builder(new HttpHost(esHost, esPort))
//                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
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


}
