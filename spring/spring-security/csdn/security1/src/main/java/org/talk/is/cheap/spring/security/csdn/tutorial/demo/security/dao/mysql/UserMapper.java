package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.dao.mysql;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.model.User;
import org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils.JsonUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class UserMapper {

    private static final List<User> users = new ArrayList<>();

    static {
        Path userDbPath = Paths.get("./user.db");

        try (BufferedReader reader = new BufferedReader(new FileReader(userDbPath.toFile()))) {

            String json = null;
            while ((json = reader.readLine()) != null) {
                User user = JsonUtil.fromJsonString(json, User.class);
                users.add(user);
            }
            log.info("users :{}", users);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User selectUserByUsername(String username) {
        Optional<User> first = users.stream().filter(u -> StringUtils.equals(u.getUsername(), username))
                .findFirst();
        return first.orElse(null);
    }

    public static void main(String[] args) {
        System.out.println(users);
    }
}
