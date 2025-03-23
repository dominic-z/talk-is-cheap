package resources.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import resources.pojo.AboutUs;
import resources.pojo.User;

import java.util.Arrays;
import java.util.List;


/*
@Controller：将该类标记为控制器。
@GetMapping("/")：处理根路径的 GET 请求。
Model model：用于向模板传递数据。
model.addAttribute()：向模型中添加数据。
return "index"：返回 index.html 模板文件。
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String index(Model model) {
        // 向模型中添加数据
        model.addAttribute("message", "欢迎使用 Thymeleaf！");
        List<String> items = Arrays.asList("项目1", "项目2", "项目3");
        model.addAttribute("items", items);
        model.addAttribute("user",new User("aa",18));
        // 返回模板文件名（不包含后缀）
        return "index1";
    }


    @GetMapping("/aboutus")
    public String aboutus(Model model) {
        // 向模型中添加数据
        model.addAttribute("aboutUs", new AboutUs("littleBear"));
        // 返回模板文件名（不包含后缀）
        return "aboutus";
    }
}