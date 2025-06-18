package resources.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * 在filter里读取请求和响应的所有内容
 * 因为请求和响应的内容只能读取一次，无法读取多次，因此需要将读取的内容缓存下来
 */
@Configuration
@Slf4j
public class HttpContentFilter extends OncePerRequestFilter {

    private static final MediaType[] LOG_CONTENT_MEDIA_TYPE = {MediaType.APPLICATION_JSON, MediaType.parseMediaType("text/*")};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("http: {}",request.getProtocol());
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            if (!headerNames.hasMoreElements()) break;
            String k = headerNames.nextElement();
            String v = request.getHeader(k);
            stringBuilder.append(String.format("%s:%s,", k, v));
        }
        log.info("request headers: {}", stringBuilder);
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);

        try {
            // ，filterChain.doFilter相当于先向这个wrapper里写入，此时response仍然是空的
            filterChain.doFilter(requestWrapper, contentCachingResponseWrapper);

            // todo: 无论报错与否，request的请求都要打印出来，懒得改了
            if (request.getMethod().equals("GET")) {
                log.info("params: {}", request.getParameterMap());
            } else if (request.getMethod().equals("POST")) {
                // 先doFilter然后打印，因为比如text/plain的请求，先打印打印不出来结果。但application/json的就可以先打印出来结果
                MediaType reqMediaType = MediaType.parseMediaType(request.getContentType());
                if (Arrays.stream(LOG_CONTENT_MEDIA_TYPE).anyMatch(t -> t.isCompatibleWith(reqMediaType))) {
                    // 或者直接用Spring提供的ContentCachingRequestWrapper
                    // 对待其他类型的请求适配不好，比如文件上传下传这种就适配不好。
                    // MyContentCachingRequestWrapper requestWrapper = new MyContentCachingRequestWrapper(request);
                    log.info("request body string : {}", ((ContentCachingRequestWrapper) requestWrapper).getContentAsString());
                } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(reqMediaType)) {
                    log.info("request body length: {}", requestWrapper.getContentLength());
                }
            }

            if (contentCachingResponseWrapper.getContentType() != null) {
                MediaType respMediaType = MediaType.parseMediaType(contentCachingResponseWrapper.getContentType());
                if (Arrays.stream(LOG_CONTENT_MEDIA_TYPE).anyMatch(t -> t.isCompatibleWith(respMediaType))) {
                    // 这里可以将响应内容读出来
                    byte[] contentAsByteArray = contentCachingResponseWrapper.getContentAsByteArray();
                    log.info("response content string : {}", new String(contentAsByteArray));
                } else {
                    log.info("response content size : {}", contentCachingResponseWrapper.getContentSize());
                }
            }

        } catch (Exception e) {
            log.error("error ", e);
        } finally {
            // 最后通过copyBodyToResponse将wrapper的内容在写入到response里，因为最后只是将response换回给前端了
            contentCachingResponseWrapper.copyBodyToResponse();

        }


    }


    // 自定义可重复读取请求体的包装类
    static class MyContentCachingRequestWrapper extends HttpServletRequestWrapper {
        private final byte[] body;

        public MyContentCachingRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            String bodyStr = getBodyString(request);
            this.body = bodyStr.getBytes(StandardCharsets.UTF_8);
        }

        public String getBodyString() {
            return new String(body, StandardCharsets.UTF_8);
        }

        private static String getBodyString(HttpServletRequest request) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body)));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CustomServletInputStream(new ByteArrayInputStream(body));
        }

        static class CustomServletInputStream extends ServletInputStream {
            private final ByteArrayInputStream inputStream;

            public CustomServletInputStream(ByteArrayInputStream inputStream) {
                this.inputStream = inputStream;
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        }
    }


}
