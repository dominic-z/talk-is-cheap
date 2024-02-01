package codegen.test;

import codegen.test.domain.pojo.Menu;
import codegen.test.domain.query.example.MenuExample;
import codegen.test.domain.query.example.RoleExample;
import codegen.test.service.MenuService;
import codegen.test.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

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
        log.info("{}", roleService.selectById(Arrays.asList(10, 11)));


        val menuExample = new MenuExample();
        val menus = menuService.selectByExample(menuExample);
        log.info("{}", menus);

        for (Menu menu : menus) {
            val pid = Menu.PidEnum.getByValue(menu.getPid());
            log.info("pidEnum: {}, value: {}, pidDescription: {}", pid, pid.getValue(), pid.getDescription());
        }
    }
}
