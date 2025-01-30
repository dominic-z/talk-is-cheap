package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao;


import org.springframework.stereotype.Repository;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.pojo.User;

@Repository
public class UserMapper {

    private final String PW ="$2a$10$CQPV0FC6Ywbxp4p584Dd/.oCB98q8fDM4SVmxOQikfZq7fNRWcrd2";

    public User selectByUsername(String username){
        User user = new User();
        user.setId(1);
        user.setUsername("pingu");
        user.setPassword(PW);
        return user;
    }


    public User selectById(int id){
        User user = new User();
        user.setId(1);
        user.setUsername("pingu");
        user.setPassword(PW);
        return user;
    }
}
