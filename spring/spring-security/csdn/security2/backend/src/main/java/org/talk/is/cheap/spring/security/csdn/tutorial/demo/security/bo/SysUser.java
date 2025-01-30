package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo;

import lombok.Data;

/**
 * 这个对象是业务对象，用来描述业务系统中的用户
 */
@Data
public class SysUser {

    private Integer id;
    private String username;
    private String password;
    private int status;
}
