package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.bo.SysUser;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Optional;

@Repository
@Slf4j
public class SysUserDao {

    private static final List<SysUser> users = new ArrayList<>();

    static {
        Path userDbPath = Paths.get("./user.db");

        try (BufferedReader reader = new BufferedReader(new FileReader(userDbPath.toFile()))) {

            String json = null;
            while ((json = reader.readLine()) != null) {
                SysUser user = JsonUtil.fromJsonString(json, SysUser.class);
                users.add(user);
            }
            log.info("users :{}", users);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<SysUser> selectUserByUsername(String username) {
        Optional<SysUser> first = users.stream().filter(u -> StringUtils.equals(u.getUsername(), username))
                .findFirst();
        return first;
    }
}
