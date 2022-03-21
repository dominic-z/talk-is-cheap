package com.example.springboot.hellospringboot;

import com.example.springboot.hellospringboot.dao.customized.CustomersDao;
import com.example.springboot.hellospringboot.dao.jdbc.template.ItemJdbcDao;
import com.example.springboot.hellospringboot.dao.mbg.CustomersMapper;
import com.example.springboot.hellospringboot.domain.pojo.Customers;
import com.example.springboot.hellospringboot.domain.query.example.CustomersExample;
import com.example.springboot.hellospringboot.service.HelloService;
import com.example.springboot.hellospringboot.service.MemoryCacheService;
import com.example.springboot.hellospringboot.service.RedisCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@SpringBootTest(classes = HelloSpringBootApplication.class)
//@RunWith(SpringRunner.class) // 没搞清楚这东西有啥用
@Slf4j(topic = "root")
class HelloSpringBootApplicationTests {

    @Autowired
    private HelloService helloService;


    @Autowired
    private ItemJdbcDao itemJdbcDao;

    @Test
    void sayHello() {
        helloService.sayHello();
    }


    @Test
    void log4jTest() {
        log.info("log test");
    }

    @Autowired
    private CustomersMapper customersMapper;
    @Autowired
    private CustomersDao customersDao;

    @Test
    public void mybatisDaoTest() {

    }

    @Test
    public void mybatisPageHelperTest() {
        // 测试mybytisPageHelper池做缓存
        List<Customers> customers = customersDao.selectCustomersRowBounds(1, 10);
        customers.stream().limit(20).forEach(customer -> log.info(customer.toString()));

        // pageHelper甚至也可以直接影响mapper类，mybatis当遇到mapper方法传入了rowBounds，就会进行分页
        // 如果没用pageHelper 默认分页方式是查询全部然后在内存里分页；如果使用了pageHelper，则会在sql层面上进行分页
        customers = customersMapper.selectByExampleWithRowbounds(new CustomersExample(), new RowBounds(1, 10));
        customers.stream().limit(20).forEach(customer -> log.info(customer.toString()));

    }


    @Autowired
    private JedisPool jedisPool;

    @Test
    public void jedisConfig() {
        // 测试jedis池做缓存

        try (
                Jedis resource = jedisPool.getResource();
        ) {
            String value = resource.get("s");
            System.out.println(value);
        }
    }


    @Autowired
    private MemoryCacheService memoryCacheService;

    @Test
    public void testMemoryCacheableAnno() {
        // 由于配置了RedisCacheConfig，因此此处会变成redis缓存，如果想测内存缓存，请将RedisCacheConfig类删掉
        log.info("read from db");
        List<Customers> customers = memoryCacheService.findCustomers();
        log.info("read from cache");
        customers = memoryCacheService.findCustomers();
//        customers.forEach(c -> log.info(c.toString()));


        log.info("read from cache put");
        customers = memoryCacheService.findCustomersPut();
        log.info("read from cache put");
        customers = memoryCacheService.findCustomersPut();
//        customers.forEach(c -> log.info(c.toString()));

        log.info("fire cache put");
        memoryCacheService.fireCustomersPut();
        log.info("read from cache");
        memoryCacheService.findCustomers();
        log.info("fire cache");
        memoryCacheService.fireCustormers();
        memoryCacheService.findCustomers();

    }


    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    public void testRedisTemplate() {
        final ValueOperations<String, String> kvOperation = redisTemplate.opsForValue();
        log.info(kvOperation.get("key"));
    }


    @Autowired
    RedisCacheService redisCacheService;

    @Test
    public void testRedisCacheAnno() {
        // 测试redis做缓存
        List<Customers> customers = redisCacheService.findCustomers();
        log.info("read from cache");

        customers = redisCacheService.findCustomers();


        List<Customers> customers2 = redisCacheService.findCustomers2();
        log.info("read from cache");

        customers2 = redisCacheService.findCustomers2();

    }


    @Test
    public void testSelectAndCacheCustomer() {
        // 测试读取并手动缓存
        final Customers customer = redisCacheService.selectAndCacheCustomer(103);

        log.info(customer.toString());

//        final Customer cacheCustomer = redisCacheService.selectAndCacheCustomer(103);
//
//        log.info(cacheCustomer.toString());

    }


}
