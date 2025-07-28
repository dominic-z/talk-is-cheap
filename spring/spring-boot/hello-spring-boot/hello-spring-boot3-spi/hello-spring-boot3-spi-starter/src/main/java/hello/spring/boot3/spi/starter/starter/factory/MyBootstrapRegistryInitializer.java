package hello.spring.boot3.spi.starter.starter.factory;

import hello.spring.boot3.spi.starter.starter.listener.MyBootstrapContextClosedEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;


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

        // 添加监听器，为的是能够触发这个监听器
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