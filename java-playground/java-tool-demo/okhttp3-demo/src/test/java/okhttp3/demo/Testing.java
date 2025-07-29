package okhttp3.demo;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Testing {


    @Test
    public void testSimpleGetRequest() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request getRequest = new Request.Builder()
//                .url("https://httpbin.org/get?test=aaa")
                .url("https://httpbin.org/not-exist?test=aaa") // 不存在的地址用于报错
                .get()
                .build();

        try (Response response = okHttpClient.newCall(getRequest).execute()) {
            System.out.println("code: " + response.code());
            System.out.println(response.headers());
            System.out.println(response.body().string());
        }
    }

    @Test
    public void testAsyncGet() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request getRequest = new Request.Builder()
                .url("https://httpbin.org/get?test=aaa")
//                .url("https://httpbin.org/not-exist?test=aaa") // 不存在的地址用于报错
//                .url("https://error-xx.org/not-exist?test=aaa") // 非法的host，用于触发报错
                .header("h1", "v1")
                .get()
                .build();

        okHttpClient.newCall(getRequest)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        System.out.printf("thread: %s %n", Thread.currentThread());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                        System.out.printf("thread: %s %n", Thread.currentThread());
                        System.out.printf("code:%n %d %n", response.code());
                        System.out.printf("response headers:%n %s %n", response.headers());
                        System.out.printf("response body:%n %s %n", response.body().string());
                    }
                });


        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testSimplePostJsonRequest() throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();


        List<String> list = List.of("k1-v1", "k1-v2");

        Map<String, String> map = Map.of("k3", "k3-v1", "k4", "k4-v1");
        Map<String, Object> bodyString = Map.of("k1", list, "k2", map, "k5", "k5-v1");

        String jsonString = JSON.toJSONString(bodyString);
        RequestBody requestBody = RequestBody.create(jsonString, MediaType.parse("application/json; charset=utf-8"));


        Request postRequest = new Request.Builder()
                .url("https://httpbin.org/post")
                .header("h1", "v1")
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(postRequest).execute()) {
            System.out.println("code: " + response.code());
            System.out.println(response.headers());
            System.out.println(response.body().string());
        }
    }


    @Test
    public void testFormDataPost() {
        OkHttpClient client = new OkHttpClient();

//        RequestBody requestBody =new FormBody(List.of("form-k"), List.of("form-v"));
        RequestBody requestBody = new FormBody.Builder()
                .add("form-k", "form-v")
                .build();
        Request request = new Request.Builder()
                .url("https://httpbin.org/post")
                .header("h1", "v1")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("code: " + response.code());
            System.out.println(response.headers());
            System.out.println(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testMultipartPost() {
        OkHttpClient client = new OkHttpClient();

        Path readmePath = Paths.get("demo.file");
        System.out.println(readmePath.toAbsolutePath());
        System.out.println(readmePath.getFileName());

        // 理解一下什么是MultipartBody，正常一个请求只会有一个body，而mulitpart相当于一次请求可以发送多种body
        MultipartBody multipartBody = new MultipartBody.Builder()
                .addFormDataPart("form-k", "form-v")
                .addPart(RequestBody.create(JSON.toJSONString(Map.of("m-k1", "m-v1")), MediaType.parse("application/json; charset=utf-8")))
                .addFormDataPart("demo.file", readmePath.getFileName().toString(), RequestBody.create(readmePath.toFile(), MediaType.parse("application/octet-stream") ))
                .build();

        Request request = new Request.Builder()
                .url("https://httpbin.org/post")
                .header("h1", "v1")
                .post(multipartBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("code: " + response.code());
            System.out.println(response.headers());
            System.out.println(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testConfigClient(){
        Path cacheDir = Paths.get("git_ignore");
        if(!Files.exists(cacheDir) || !Files.isDirectory(cacheDir)){
            try {
                Files.createDirectory(cacheDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor())
                .cache(new Cache(cacheDir.toFile(), 1024))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();


        Request getRequest = new Request.Builder()
                .url("https://httpbin.org/get?test=aaa")
                .get()
                .build();

        for(int i=0;i<2;i++){
            try (Response response = client.newCall(getRequest).execute()) {
                System.out.println("code: " + response.code());
                System.out.println(response.headers());
                System.out.println(response.body().string());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Test
    public void testStreamBody(){
        RequestBody requestBody = new RequestBody() {
            @Override public MediaType contentType() {
//                return MediaType.parse("text/x-markdown; charset=utf-8");
                return MediaType.parse("application/octet-stream");
            }

            @Override public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                for (int i = 2; i <= 10; i++) {
                    sink.writeUtf8(" * \n");
                }
            }

        };

        Request request = new Request.Builder()
                .url("https://httpbin.org/post")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(request).execute()) {
            System.out.println("code: " + response.code());
            System.out.println(response.headers());
            System.out.println(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }



    @Test
    public void testDispatcher(){
        Dispatcher dispatcher = new Dispatcher();

        // 设置最大并发请求数量
        dispatcher.setMaxRequests(64);

        // 设置对单个主机的最大并发请求数量
        dispatcher.setMaxRequestsPerHost(5);

        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .build();

        // 使用 client 执行请求
    }


    @Test
    public void testConnectionPool(){
        // 配置连接池
        ConnectionPool connectionPool = new ConnectionPool(
                10, // 最大空闲连接数
                5, // 保持连接的时间
                TimeUnit.MINUTES
        );
    }
}
