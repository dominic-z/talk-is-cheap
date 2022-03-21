package org.example.spring.starter.client.interceptor;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import org.example.spring.starter.log.Loggers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author dominiczhu
 * @version 1.0
 * @title OkHttpInterceptor
 * @date 2022/2/15 10:48 上午
 */
public class OkHttpInterceptor implements Interceptor {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    @Override
    public Response intercept(Chain chain) throws IOException {

        final Request request = chain.request();

        String reqParameter = "";
        String targetUrl = "";
        String method = "";
        try {
            targetUrl = request.url().url().toString();
            method = request.method();
            if ("GET".equals(method)) {
                reqParameter = request.url().query();
            } else if ("POST".equals(request.method())) {
                final RequestBody reqBody = request.body();
                if (reqBody != null) {
                    final Buffer buffer = new Buffer();
                    reqBody.writeTo(buffer);

                    MediaType contentType = reqBody.contentType();
                    Charset charset = UTF_8;
                    if (contentType != null) {
                        charset = contentType.charset(UTF_8);
                    }
                    reqParameter = buffer.readString(charset);
                }
            }
        } catch (Exception e) {
            Loggers.ERROR_LOG.error("error when read request params", e);
        }

        final Response resp = chain.proceed(request);

        String respParameter = "";
        try {
            final ResponseBody responseBody = resp.body();

            if (HttpHeaders.hasBody(resp) && responseBody != null) {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.getBuffer();

                Charset charset = UTF_8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF_8);
                }
                respParameter = buffer.clone().readString(charset);
            }
        } catch (Exception e) {
            Loggers.ERROR_LOG.error("error when read response params", e);
        }

        Loggers.CLIENT_LOG.info("{},{},{},{}", targetUrl, method, reqParameter, respParameter);

        return resp;
    }
}
