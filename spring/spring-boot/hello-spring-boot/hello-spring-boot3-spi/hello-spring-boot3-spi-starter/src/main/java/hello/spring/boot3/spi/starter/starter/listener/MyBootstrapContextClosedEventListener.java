package hello.spring.boot3.spi.starter.starter.listener;

import hello.spring.boot3.spi.starter.starter.factory.MyBootstrapRegistryInitializer;
import org.springframework.boot.BootstrapContextClosedEvent;
import org.springframework.context.ApplicationListener;


/**
 * 检测BootstrapContextClosedEvent，将在BootstrapContext容器中的对象糯稻ApplicationContext里
 */
public class MyBootstrapContextClosedEventListener implements ApplicationListener<BootstrapContextClosedEvent> {
    @Override
    public void onApplicationEvent(BootstrapContextClosedEvent event) {
//        你会发现这个事件打印在出现spring图标的下面
        System.out.println("====================MyBootstrapContextClosedEvent");
        MyBootstrapRegistryInitializer.MyEarlyComponent component =
                event.getBootstrapContext().get(MyBootstrapRegistryInitializer.MyEarlyComponent.class);

        event.getApplicationContext().getBeanFactory()
                .registerSingleton("myEarlyComponent",component);
    }
}