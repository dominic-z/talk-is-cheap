package testing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public class SpringTesting {


    @Test
    public void testEncrypted(){

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode("1234abcd");
        log.info("{}",encode);
//        好像每次encode的结果都不太一样

        boolean matches = bCryptPasswordEncoder.matches("1234abcd", encode);
        log.info("{}",matches);
    }
}
