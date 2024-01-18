package codegen.test;

import codegen.test.domain.query.example.RoleExample;
import codegen.test.service.MenuService;
import codegen.test.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    RoleService roleService;
    @Autowired
    MenuService menuService;

    @Override
    public void run(String... args) throws Exception {
        RoleExample roleExample = new RoleExample();

        roleExample.createCriteria().andIdEqualTo(10);
        log.info("{}", roleService.selectByExample(roleExample));
    }
}
