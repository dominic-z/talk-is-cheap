import com.alibaba.fastjson.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author dominiczhu
 * @version 1.0
 * @title HttpClientDemo
 * @date 2021/5/26 上午11:35
 */
public class HttpClientDemo {


    @Test
    public void testGet() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://httpbin.org/get");

        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                System.out.println("resp headers: " + Arrays.toString(response.getAllHeaders()));

                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                System.out.println("resp body: " + content);
                System.out.println("content length: " + content.length());
            }
        } finally {
            if (response != null) {
                response.close();
            }
            //相当于关闭浏览器
            httpclient.close();
        }
    }

    @Test
    public void testParamGet() throws URISyntaxException, IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        URI uri = new URIBuilder("http://httpbin.org/get").setParameter("wd", "java").build();

        HttpGet httpGet = new HttpGet(uri);

        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                System.out.println("resp headers: " + Arrays.toString(response.getAllHeaders()));

                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                System.out.println("resp body: " + content);
                System.out.println("content length: " + content.length());
            }
        } finally {
            if (response != null) {
                response.close();
            }
            //相当于关闭浏览器
            httpclient.close();
        }
    }

    @Test
    public void testPostForm() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://httpbin.org/post");

        List<NameValuePair> parameters = new ArrayList<>(0);
        parameters.add(new BasicNameValuePair("scope", "project"));
        parameters.add(new BasicNameValuePair("q", "java"));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        // 将请求实体设置到httpPost对象中
        httpPost.setEntity(formEntity);

        httpPost.setEntity(new StringEntity("name=vip&q=java", ContentType.APPLICATION_FORM_URLENCODED));

        httpPost.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                System.out.println("resp headers: " + Arrays.toString(response.getAllHeaders()));

                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                System.out.println("resp body: " + content);
                System.out.println("content length: " + content.length());
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }

    @Test
    public void testPostJSON() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://httpbin.org/post");

        // 将请求实体设置到httpPost对象中

        JSONObject reqBody = new JSONObject();
        reqBody.put("name", "vip");
        httpPost.setEntity(new StringEntity(reqBody.toJSONString(), ContentType.APPLICATION_JSON));

        httpPost.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                System.out.println("resp headers: " + Arrays.toString(response.getAllHeaders()));

                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                System.out.println("resp body: " + content);
                System.out.println("content length: " + content.length());
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }


    @Test
    public void configPost() throws IOException {

        // 连接池核心配置
        final int MAX_TOTAL_CONNECTIONS = 200;        // 连接池最大总连接数
        final int MAX_PER_ROUTE_CONNECTIONS = 50;     // 每个路由（域名）的最大连接数
        final int IDLE_CONNECTION_TIMEOUT = 30000;     // 空闲连接超时时间（30秒）

        // 1. 配置连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        // 设置连接池最大总连接数
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        // 设置每个路由（比如 www.xxx.com）的最大连接数
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE_CONNECTIONS);
// 可选：为特定路由单独设置最大连接数（比如对某个高频接口放宽限制）
        // HttpHost host = new HttpHost("api.example.com", 80);
        // connectionManager.setMaxPerRoute(new HttpRoute(host), 80);

        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connectionManager).build();
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(10000).build();


        HttpPost httpPost = new HttpPost("http://httpbin.org/post");

        // 将请求实体设置到httpPost对象中

        JSONObject reqBody = new JSONObject();
        reqBody.put("name", "vip");
        httpPost.setEntity(new StringEntity(reqBody.toJSONString(), ContentType.APPLICATION_JSON));
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpclient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                System.out.println("resp headers: " + Arrays.toString(response.getAllHeaders()));

                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                System.out.println("resp body: " + content);
                System.out.println("content length: " + content.length());
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }


    /**
     * 配置ssl，主要参考豆包：https://www.doubao.com/thread/w8357ec83e74699a0
     *
     * 证书来自于hello-spring-boot-starter-web这个工程
     *
     *
     * @throws IOException
     * @throws URISyntaxException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    @Test
    public void configClient() throws IOException, URISyntaxException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        final int CONNECT_TIMEOUT = 10000;    // 连接超时：5秒（建立TCP连接的超时）
        final int SOCKET_TIMEOUT = 120000;    // 读取超时：120秒（等待响应数据的超时）
        final int CONNECTION_REQUEST_TIMEOUT = 3000; // 从连接池获取连接的超时：3秒

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .build();


        KeyStore keyStore = KeyStore.getInstance("PKCS12"); // 直接指定PKCS12格式，这个方便导入，如果使用其他格式的文件，比较麻烦
        File p12File = new File(HttpClientDemo.class.getResource("server.p12").getPath());
        try (FileInputStream instream = new FileInputStream(p12File)) {
            keyStore.load(instream, "123456".toCharArray()); // .p12文件密码，证书来自于另一个工程
        }

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(keyStore, null) // 加载PKCS12证书作为信任库
                .build();

        // 3. 创建SSL连接工厂，禁用主机名验证（避免Hostname不匹配错误）
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE // 忽略主机名校验
        );

        // 4. 配置HttpClient，使用自定义SSL工厂

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultRequestConfig(requestConfig).build();

// 2. 构建请求URL和参数
        String baseUrl = "https://localhost:8082/timeConsumingJob"; // 基础URL
        URIBuilder uriBuilder = new URIBuilder(baseUrl);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("duration", "70"));       // 参数1：id=123
        uriBuilder.addParameters(params); // 将参数添加到URL

        // 4. 构建URI和HttpGet请求
        URI uri = uriBuilder.build();

        HttpGet httpGet = new HttpGet(uri);


        // 将请求实体设置到httpPost对象中

        httpGet.setConfig(requestConfig);
        httpGet.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        CloseableHttpResponse response = null;
        try {
            // 执行请求

            response = httpClient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                //请求体内容
                System.out.println("resp headers: " + Arrays.toString(response.getAllHeaders()));

                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                //内容写入文件
                System.out.println("resp body: " + content);
                System.out.println("content length: " + content.length());
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpClient.close();
        }

        // 死循环
        while (true){

        }
    }
}
