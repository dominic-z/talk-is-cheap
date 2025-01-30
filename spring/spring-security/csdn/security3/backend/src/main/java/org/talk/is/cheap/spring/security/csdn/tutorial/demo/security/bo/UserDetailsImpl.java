package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pojo.User;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor    // 这三个注解可以帮我们自动生成 get、set、有参、无参构造函数
public class UserDetailsImpl implements UserDetails {

    private User user;	// 通过有参构造函数填充赋值的

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {  // 检查账户是否 没过期。
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {   // 检查账户是否 没有被锁定。
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {  //检查凭据（密码）是否 没过期。
        return true;
    }

    @Override
    public boolean isEnabled() {    // 检查账户是否启用。
        return true;
    }

    // 这个方法是 @Data注解 会自动帮我们生成，用来获取 loadUserByUsername 中最后我们返回的创建UserDetailsImpl对象时传入的User。
    // 如果你的字段包含 username和password 的话可以用强制类型转换, 把 UserDetailsImpl 转换成 User。如果不能强制类型转换的话就需要用到这个方法了
    public User getUser() {
        return user;
    }
}

