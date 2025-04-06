package org.example.spring.starter.log.web;

import org.example.spring.starter.log.Loggers;
import org.example.spring.starter.log.utils.JacksonUtil;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ServiceRequestFilter
 * @date 2022/2/14 9:54 上午
 */
public class ServerLogRequestFilter extends OncePerRequestFilter {
    private static final String[] excludeUrlPatterns = {
            "/actuator",
            "/actuator/**"
    };

    private static final AntPathMatcher matcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        // todo: 应该将需要打印的东西提取出来作为一个独立的对象，如果请求错误，也要把内容打印出来
        // 这个类的实现方式非常赞，因为这个HttpServletRequest一定会在某个地方对inputStream进行read
        // 那么就对InputStream的read进行包装，第一次读取的时候将结果缓存下来，然后第二次再读取的时候就重新生成一个stream
        // 包装类的典范
        final ContentCachingRequestWrapper reqWrapper = new ContentCachingRequestWrapper(request);
        final ContentCachingResponseWrapper respWrapper = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(reqWrapper, respWrapper);

        try{
            final String srcAddr = reqWrapper.getRemoteAddr();
            final String requestURI = reqWrapper.getRequestURI();
            final String method = reqWrapper.getMethod();
            final String formatReqBody =
                    format(new String(reqWrapper.getContentAsByteArray(),
                            reqWrapper.getCharacterEncoding()));
            final Map<String, String[]> parameterMap = request.getParameterMap();

            final String formatParameterMap = format(JacksonUtil.MAPPER.writeValueAsString(parameterMap));

            final String formatRespBody =
                    format(new String(respWrapper.getContentAsByteArray(), respWrapper.getCharacterEncoding()));
            Loggers.SERVER_LOG.info("{},{},{},{},{},{}", srcAddr, requestURI, method, formatReqBody, formatParameterMap,
                    formatRespBody);
        }finally {
            respWrapper.copyBodyToResponse();
        }
    }

    private String format(String s) {
        return s.replaceAll(" +|\n|\r", "");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        for (String excludeUrlPattern : excludeUrlPatterns) {
            if (matcher.match(excludeUrlPattern, request.getRequestURI())) {
                return false;
            }
        }
        return super.shouldNotFilter(request);
    }
}
