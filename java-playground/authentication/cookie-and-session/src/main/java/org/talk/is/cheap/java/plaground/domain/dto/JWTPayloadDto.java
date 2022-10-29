package org.talk.is.cheap.java.plaground.domain.dto;

import lombok.Data;

import java.util.Date;

@Data
public class JWTPayloadDto {

    private String iss;
    private String sub;
    private String aud;
    private Date exp;
    private Date nbf;
    private Date iat;
    private String id;

    private String loginName;

}
