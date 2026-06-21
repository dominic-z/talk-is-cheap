package hello.spring.boot3.spi.starter.starter.listener;

import hello.spring.boot3.spi.starter.starter.factory.MyBootstrapRegistryInitializer;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * 解释一下SpringApplicationRunListener的作用
 * https://www.qianwen.com/share/chat/6ddf0e6518e7427f9fc88a0d0f9d3263
 *
 * 监听并干预 Spring Boot 应用从 main() 方法开始到 ApplicationContext 完全启动的全过程。
 */

public class MyBoostrapRegistryInitializerApplicationRunListener implements SpringApplicationRunListener {

    /**
     * 这个是spring**应用**启动的第一步，不过这个时候BootstrapContext已经完成，可以看到这里可以使用MyBootstrapRegistryInitializer创建的对象
     * @param bootstrapContext the bootstrap context
     */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        System.out.println("====================MyBoostrapRegistryInitializerApplicationRunListener");
        SpringApplicationRunListener.super.starting(bootstrapContext);

        MyBootstrapRegistryInitializer.MyEarlyComponent component =
                bootstrapContext.get(MyBootstrapRegistryInitializer.MyEarlyComponent.class);

        // 使用组件
        component.doSomething();
    }

}