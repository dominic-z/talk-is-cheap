package testing;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.SysUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pw.CustomMd5PasswordEncoder;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;

import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class ApplicationTest {
    @Test
    public void testMd5() throws JsonProcessingException {

        String rawPassword = "abcd1234";
        byte[] md5Digest = DigestUtils.md5Digest(rawPassword.getBytes());
        log.info("md5: {}", Arrays.toString(md5Digest));
        String md5Base64 = Base64.getEncoder().encodeToString(md5Digest);
        log.info("md5Base64: {}", md5Base64);

        byte[] decode = Base64.getDecoder().decode(md5Base64);
        log.info("md5: {}", Arrays.toString(decode));


        SysUser sysUser = new SysUser();
        sysUser.setId(1);
        sysUser.setStatus(1);
        sysUser.setUsername("pingu");
        sysUser.setPassword(md5Base64);
        log.info("pingu: {}",JsonUtil.toJsonString(sysUser));

    }

    @Test
    public void generateSysUser() throws JsonProcessingException {
        CustomMd5PasswordEncoder encoder = new CustomMd5PasswordEncoder();

        SysUser sysUser = new SysUser();
        sysUser.setUsername("pingu");
        sysUser.setPassword(encoder.encode("robin"));
        log.info(JsonUtil.toJsonString(sysUser));
    }
}
