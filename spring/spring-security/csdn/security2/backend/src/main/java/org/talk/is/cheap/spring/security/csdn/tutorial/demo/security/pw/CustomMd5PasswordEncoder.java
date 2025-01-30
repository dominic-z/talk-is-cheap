package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pw;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.Base64;

public class CustomMd5PasswordEncoder implements PasswordEncoder {
    @Override
    public String encode(CharSequence rawPassword) {

        byte[] md5Digest = DigestUtils.md5Digest(rawPassword.toString().getBytes());
//        教程里直接用Arrays.toString来作为编码后的密码，个人觉得可以优化下，用base64
//        String parsedMd5 = Arrays.toString(md5Digest);
        String parsedMd5 = Base64.getEncoder().encodeToString(md5Digest);
        return parsedMd5;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
//你可以在这打个断点，就能看到在哪唤起的密码比对
        return StringUtils.equals(this.encode(rawPassword),encodedPassword);
    }
}
