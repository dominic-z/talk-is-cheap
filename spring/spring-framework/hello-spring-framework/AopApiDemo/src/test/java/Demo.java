import advice.MyAdvisor;
import advice.MyAfterReturningAdvice;
import advice.MyBeforeAdvice;
import advice.MyPointCut;
import domain.Author;
import domain.Blog;
import org.junit.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.aop.target.PoolingConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import service.AuthorService;
import service.BlogService;
import service.BlogServiceImp;

import java.util.Arrays;

public class Demo {
    @Test
    public void testXML() {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("classpath*:aopApiContext.xml");

        Author author = ac.getBean("author", Author.class);
//        System.out.println(author);
//
//        Blog blog = ac.getBean("blog", Blog.class);
//        System.out.println(blog);

        System.out.println("-------使用JDK代理-------");
        BlogService blogService = ac.getBean("blogServiceProxy", BlogService.class);
        blogService.getBlog(author);
        System.out.println("------------由于PointCut前置通知未触发------------");
        blogService.getBlog();
        System.out.println("-------使用CGLIB代理-------");
        AuthorService authorService = ac.getBean("authorServiceProxy", AuthorService.class);
        authorService.getAuthor();
    }

    @Test
    public void testAOPApiProgrammatically() {
        System.out.println("测试编程式动态代理");
        BlogService blogService = new BlogServiceImp();
        ProxyFactory factory = new ProxyFactory();
        factory.setTarget(blogService);
        factory.setInterfaces(BlogService.class);

        DefaultPointcutAdvisor advisor = new MyAdvisor();
        advisor.setPointcut(new MyPointCut());
        advisor.setAdvice(new MyBeforeAdvice());
        factory.addAdvisor(advisor);

        factory.addAdvice(new MyAfterReturningAdvice());
        factory.setFrozen(true);
//        factory.addAdvice(new MyAfterReturningAdvice());//会报错，因为工厂已经被冻结，不可更改了
        BlogService blogServiceProxy = (BlogService) factory.getProxy();
        blogServiceProxy.getBlog(new Author());

        Advised advised = (Advised) blogServiceProxy;
        Advisor[] advisors = advised.getAdvisors();
        System.out.println(Arrays.toString(advisors));
    }

    @Test
    public void testBeanNameAutoProxy() {
        System.out.println("测试BeanNameAutoProxy自动代理，即对object进行自动代理");

        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("beanNameAutoAopContext.xml");

        Author author = ac.getBean("author", Author.class);

        System.out.println("-------自动生成代理对象-------");
        BlogService blogService = ac.getBean("blogServiceImp", BlogService.class);
        blogService.getBlog(author);
        System.out.println("------------由于PointCut前置通知未触发------------");
        blogService.getBlog();
    }

    @Test
    public void testDefaultAdvisorAutoProxy() {
        System.out.println("测试DefaultAdvisorAutoAop自动代理，即对object进行自动代理");

        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("defaultAdvisorAutoAopContext.xml");

        Author author = ac.getBean("author", Author.class);

        System.out.println("-------自动生成代理对象-------");
        BlogService blogService = ac.getBean("blogServiceImp", BlogService.class);
        blogService.getBlog(author);
        System.out.println("------------由于PointCut前置通知未触发------------");
        blogService.getBlog();
    }

    @Test
    public void testSwappableTargetResource() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("swappableTargetResourceContext.xml");
        System.out.println("----------测试HotSwappableTargetSource----------");

        AuthorService currentAuthorService = (AuthorService) ac.getBean("swappableAuthorService");
        System.out.println("----------before swap----------");
        currentAuthorService.getAuthor();
        System.out.println(currentAuthorService.getId());//会触发一次after advice

        HotSwappableTargetSource swapper = (HotSwappableTargetSource) ac.getBean("swapper");
        AuthorService newAuthorService = ac.getBean("authorService2", AuthorService.class);
        swapper.swap(newAuthorService);
        System.out.println("----------after swap----------");
        currentAuthorService.getAuthor();
        System.out.println(currentAuthorService.getId());//会触发一次after advice
    }

    @Test
    public void testPoolingTargetResource() throws Exception {
        ApplicationContext ac = new ClassPathXmlApplicationContext("poolingTargetResourceContext.xml");
        System.out.println("----------测试PoolingTargetSource----------");
        Author author = ac.getBean("author", Author.class);

        BlogService poolingTarget1 = (BlogService) ac.getBean("poolingTarget1");
        poolingTarget1.getBlog(author);//通过debug可以看到，执行过程完成后，会在CommonsPool2TargetSource里自动release掉这个target
        //这个target就是这个poolingTarget1的BlogService

        System.out.println("----------测试PoolingConfig----------");
        PoolingConfig poolingTarget2 = (PoolingConfig) ac.getBean("poolingTarget2");
        ((BlogService)poolingTarget2).getBlog(author);
        System.out.println("Maxsize: " + poolingTarget2.getMaxSize());
        System.out.println("Active Count: " + poolingTarget2.getActiveCount());
//        虽然返回的是1，但此时实际上已经被释放了，可以通过下面CommonsPool2TargetSource实例的第一个getActiveCount看出

        System.out.println("----------测试CommonsPool2TargetSource----------");
        CommonsPool2TargetSource poolTargetSource= (CommonsPool2TargetSource) ac.getBean("poolTargetSource");
        System.out.println("Active count:"+poolTargetSource.getActiveCount());
        BlogService target1 = (BlogService) poolTargetSource.getTarget();
        BlogService target2 = (BlogService) poolTargetSource.getTarget();
        System.out.println("Active count:"+poolTargetSource.getActiveCount());
        poolTargetSource.releaseTarget(target1);//手动释放1个
        System.out.println("Active count:"+poolTargetSource.getActiveCount());

    }
}
