package org.talk.is.cheap.java.plaground.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JWTService {

    @Value("${secret.key.jwt}")
    private String secretKeyInBase64;

    private static final SignatureAlgorithm hs256 = SignatureAlgorithm.HS256;

    public static final String JWT_COOKIE_NAME = "jwt";
    public static final String JWT_LOGIN_NAME = "loginName";

    private SecretKeySpec secretKey;

    @PostConstruct
    private void init() {
        secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKeyInBase64), hs256.getJcaName());
    }

    private static final int periodOfValidityInMinute = 1;

    public String getToken(String loginName) {

        Date now = new Date();
        Date exp = DateUtil.offset(now, DateField.MINUTE, periodOfValidityInMinute);

        String tokenId = UUID.randomUUID().toString();
        JwtBuilder builder = Jwts.builder()
                .setId(tokenId)
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", hs256.getValue())
                .setIssuedAt(now)
                .setExpiration(exp)
                .setSubject("org.talk.is.cheap")
                .claim(JWT_LOGIN_NAME, loginName)
                .signWith(secretKey, hs256);
        return builder.compact();
    }


    public Jwt<?, ?> validateToken(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null || cookies.length == 0) {
            log.info("cookie is null");
            throw new RuntimeException("cookie is null");
        }

        log.info("validate token, cookie: {}", Arrays.toString(cookies));
        for (Cookie cookie : cookies) {
            if (JWTService.JWT_COOKIE_NAME.equals(cookie.getName())) {
                log.info("find jwt, raw jwt: {}", cookie.getValue());

                Jwt jwt = this.validateToken(cookie.getValue());
                if (!(jwt.getBody() instanceof Claims)) {
                    log.error("jwt body: {}", jwt.getBody());
                    throw new IncorrectClaimException(jwt.getHeader(), null, "jwt.getBody() is not claims");
                }
                Claims claims = ((Claims) jwt.getBody());
                Object loginName = claims.get(JWT_LOGIN_NAME);
                if(loginName == null){
                    throw new IncorrectClaimException(jwt.getHeader(),claims,"loginName is Blank");
                }
                log.info("oh it's {}", loginName);

                return jwt;
            }
        }
        throw new RuntimeException("cookie is null");
    }

    public Jwt<?, ?> validateToken(String token) {

        return Jwts.parserBuilder().setSigningKey(secretKey).build().parse(token);

    }

}
