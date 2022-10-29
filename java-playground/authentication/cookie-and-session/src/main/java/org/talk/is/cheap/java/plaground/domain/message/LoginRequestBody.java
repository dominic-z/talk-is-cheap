package org.talk.is.cheap.java.plaground.domain.message;

import lombok.Data;

@Data
public class LoginRequestBody {
    private String username;
    private String password;
}
