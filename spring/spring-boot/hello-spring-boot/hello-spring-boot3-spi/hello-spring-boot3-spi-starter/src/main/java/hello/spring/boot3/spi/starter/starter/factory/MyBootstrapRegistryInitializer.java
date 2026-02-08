package hello.spring.boot3.spi.starter.starter.factory;

import hello.spring.boot3.spi.starter.starter.listener.MyBootstrapContextClosedEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;


/**
 * BootstrapRegistryInitializer 是 Spring Boot 2.4+ 引入的一个新扩展接口，用于在 Spring Boot 应用启动的最早期阶段（甚至早于 Environment 创建）向 BootstrapRegistry 中注册组件。
 * 它的主要目的是支持 配置数据（Config Data）机制 和 早期初始化逻辑，尤其是在云原生、配置中心（如 Spring Cloud Config、Nacos、Vault）等场景中。
 *
 * 在 Spring Boot 2.4 之前，像 Spring Cloud 这样的项目需要在 Environment 准备好之前就加载外部配置（比如从 Config Server 拉取 application.yml），
 * 但原有的扩展点（如 EnvironmentPostProcessor）太晚了——因为 Environment 已经部分构建完成。
 *
 * https://www.qianwen.com/share/chat/262343b0ef694a509038958a9614d4b5
 *
 */
@Slf4j
public class MyBootstrapRegistryInitializer implements BootstrapRegistryInitializer {

    @Override
    public void initialize(BootstrapRegistry registry) {
        // 注册一个自定义组件，延迟初始化
        registry.register(MyEarlyComponent.class, context -> {
            // 可以从BootstrapContext获取其他已注册的组件
            SomeDependency dependency = context.get(SomeDependency.class);
            return new MyEarlyComponent(dependency);
        });

        // 注册必要的依赖组件
        registry.register(SomeDependency.class, context -> new SomeDependency());

        // 添加监听器，为的是能够触发这个监听器，bootstrapContext会被销毁
        registry.addCloseListener(new MyBootstrapContextClosedEventListener());
    }

    // 自定义早期组件
    public static class MyEarlyComponent {
        private final SomeDependency dependency;

        MyEarlyComponent(SomeDependency dependency) {
            this.dependency = dependency;
        }

        // 组件功能方法
        public void doSomething() {
            // 早期初始化逻辑
//            得用sout，因为这时候log对象还不存在
            System.out.println("MyBootstrapRegistryInitializer do something");
        }
    }

    // 依赖组件
    static class SomeDependency {
        // 依赖组件的实现
    }
}