package resources.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * 在filter里读取请求和响应的所有内容
 * 因为请求和响应的内容只能读取一次，无法读取多次，因此需要将读取的内容缓存下来
 */
@Configuration
@Slf4j
public class HttpContentFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 或者直接用Spring提供的ContentCachingRequestWrapper
        MyContentCachingRequestWrapper myContentCachingRequestWrapper = new MyContentCachingRequestWrapper(request);

        StringBuilder stringBuilder = new StringBuilder();
        Enumeration<String> headerNames = myContentCachingRequestWrapper.getHeaderNames();
        while (true){
            if (!headerNames.hasMoreElements()) break;
            String k = headerNames.nextElement();
            String v = myContentCachingRequestWrapper.getHeader(k);
            stringBuilder.append(String.format("%s:%s,",k,v));
        }
        log.info("request headers: {}",stringBuilder);
        log.info("request body string : {}",myContentCachingRequestWrapper.getBodyString());

        // ContentCachingResponseWrapper 的实现不太一样
        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        // ，filterChain.doFilter相当于先向这个wrapper里写入，此时response仍然是空的
        filterChain.doFilter(myContentCachingRequestWrapper,contentCachingResponseWrapper);
        // 这里可以将响应内容读出来
        byte[] contentAsByteArray = contentCachingResponseWrapper.getContentAsByteArray();
        log.info("response content string : {}",new String(contentAsByteArray));

        // 最后通过copyBodyToResponse将wrapper的内容在写入到response里，因为最后只是将response换回给前端了
        contentCachingResponseWrapper.copyBodyToResponse();



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
