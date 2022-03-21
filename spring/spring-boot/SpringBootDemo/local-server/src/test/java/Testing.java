import org.apache.commons.codec.digest.HmacUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Testing
 * @date 2021/8/11 下午4:06
 */
public class Testing {

    private String makeAuthorization() throws Exception {
        long timestamp = System.currentTimeMillis();
        int nonce = new Random().nextInt(10 * 8) + 1;
        Base64 base64 = new Base64();
        String secureId="BARlMqjZVv6L7Y9UY4DvmRQ6HWI4ESdfhzkL";
        String secureKey="tKKSBmi9ixUKHyYSBz48GOzCWMOlwi34";
        byte[] baseStr = base64.encode(HmacUtils.hmacSha1(secureKey, secureId + timestamp + nonce));
        String signature = URLEncoder.encode(new String(baseStr), StandardCharsets.UTF_8.toString());
        String accessToken = "TBDS " + secureId + " " + timestamp + " " + nonce + " " + signature;

        return accessToken;
    }

    @Test
    public void test() throws Exception {
        System.out.println(makeAuthorization());
    }
}
