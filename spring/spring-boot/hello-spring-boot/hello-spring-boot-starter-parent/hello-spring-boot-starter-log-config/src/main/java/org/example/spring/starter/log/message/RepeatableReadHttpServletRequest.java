package org.example.spring.starter.log.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author dominiczhu
 * @version 1.0
 * @title RepeatablReadHttpServeletRequest
 * @date 2022/2/14 1:10 下午
 */
public class RepeatableReadHttpServletRequest extends HttpServletRequestWrapper {

    private CachedServletInputStream cachedServletInputStream;

    public RepeatableReadHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    private static class CachedServletInputStream extends ServletInputStream {

        private ServletInputStream inputStream;

        public CachedServletInputStream(ServletInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public boolean isFinished() {
            return this.inputStream.isFinished();
        }

        @Override
        public boolean isReady() {
            return this.inputStream.isReady();
        }

        @Override
        public void setReadListener(ReadListener listener) {
            this.inputStream.setReadListener(listener);
        }

        @Override
        public int read() throws IOException {
            return this.inputStream.read();
        }

        @Override
        public int readLine(byte[] b, int off, int len) throws IOException {
            return super.readLine(b, off, len);
        }
    }
}
