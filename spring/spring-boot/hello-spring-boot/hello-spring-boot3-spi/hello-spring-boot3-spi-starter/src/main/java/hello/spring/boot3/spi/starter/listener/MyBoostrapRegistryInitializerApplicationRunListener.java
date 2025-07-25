package hello.spring.boot3.spi.starter.listener;

import hello.spring.boot3.spi.starter.factory.MyBootstrapRegistryInitializer;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * 来自都报
 */

public class MyBoostrapRegistryInitializerApplicationRunListener implements SpringApplicationRunListener {

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