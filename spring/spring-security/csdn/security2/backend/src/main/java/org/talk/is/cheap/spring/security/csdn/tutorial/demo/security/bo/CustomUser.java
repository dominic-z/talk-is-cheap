package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 用来描述security中的UserDetail对象，这个User就是spring里面内置的一个UserDetail
 */
public class CustomUser extends User {
    /**
     * 不应该提供setter方法才对
     */
    @Getter
    private final SysUser sysUser;
    public CustomUser(SysUser sysUser, Collection<? extends GrantedAuthority> authorities) {
        super(sysUser.getUsername(), sysUser.getPassword(), authorities);
        this.sysUser = sysUser;
    }
}
