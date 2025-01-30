package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.CustomUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.SysUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.SysUserDao;

import java.util.*;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

//    我给改成了直接调用dao，博客里用的SysuserService，如果用这个会产生循环依赖导致无法启用应用
    @Autowired
    private SysUserDao sysUserDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SysUser> optionalSysUser = sysUserDao.selectUserByUsername(username);
        if(optionalSysUser.isEmpty()){
            throw new UsernameNotFoundException(String.format("%s is not found",username));
        }
        SysUser sysUser = optionalSysUser.get();
        if(sysUser.getStatus()==0){
            throw new RuntimeException(String.format("%s is invalid，账号已经注销",username));
        }

//        这段没啥用，这代码是登录接口触发的，登录时根本不需要授权，而是在登录完成后拿着token去访问授权的，所以写在这没用。应该写在filter里
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = Arrays.asList(
                new SimpleGrantedAuthority("bnt.sysRole.list"));

        return new CustomUser(sysUser, simpleGrantedAuthorities);


    }
}
