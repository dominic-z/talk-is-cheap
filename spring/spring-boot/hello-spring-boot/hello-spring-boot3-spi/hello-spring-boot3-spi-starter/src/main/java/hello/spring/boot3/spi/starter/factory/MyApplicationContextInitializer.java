package hello.spring.boot3.spi.starter.factory;

import hello.spring.boot3.spi.starter.service.impl.HijServiceImpl;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 容器准备就绪之前，对容器再做一些自定义
 */
public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        System.out.println("对applicationContext容器作一些处理");
        applicationContext
                .getBeanFactory()
                .registerSingleton("hijServiceImpl",new HijServiceImpl());

        // 1. 修改上下文的环境配置
        applicationContext.getEnvironment().getSystemProperties().put("app.initialized", "true");

        // 2. 注册Bean工厂后置处理器（用于修改Bean定义）
//        applicationContext.addBeanFactoryPostProcessor(beanFactory -> {
//            // 例如：动态注册一个Bean定义
//            // beanFactory.registerBeanDefinition("myBean", new RootBeanDefinition(MyBean.class));
//        });

        // 3. 设置上下文的一些属性
        applicationContext.setId("my-custom-context-id");
        System.out.println("上下文初始化器执行完成，ID: " + applicationContext.getId());
    }
}
