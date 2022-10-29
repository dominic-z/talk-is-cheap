package org.talk.is.cheap.java.plaground;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


@Slf4j
public class Testing {

    @Test
    public void createJWT() {
        SignatureAlgorithm hs256 = SignatureAlgorithm.HS256;
        Date now = new Date();
        DateTime expireDate = DateUtil.offsetMinute(now, 10);
        SecretKey secretKey = Keys.secretKeyFor(hs256);

        byte k = 121;

        String keyInBase64 = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        byte[] decode = Base64.getDecoder().decode(keyInBase64);
        log.info("decode: {}", decode);
        log.info("secret key: {}, {}", keyInBase64, Arrays.toString(secretKey.getEncoded()));
        String tokenId = UUID.randomUUID().toString();
        JwtBuilder builder = Jwts.builder()
                .setId(tokenId)
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", hs256.getValue())
                .setIssuedAt(now)
                .setSubject("test")
                .setExpiration(expireDate)
                .claim("name", "little pig")
                .claim("mobile", "13912345678")
                .signWith(secretKey, hs256);
        String jwt = builder.compact();
        log.info("jwt: {}", jwt);

//        SecretKey wrongSecretKey = Keys.secretKeyFor(hs256);

        Jwt parsedJwt = Jwts.parserBuilder().setSigningKey(secretKey).build().parse(jwt);

        log.info("parsedJwt: {}, bodyClass: {}", parsedJwt,parsedJwt.getBody());
    }
}
