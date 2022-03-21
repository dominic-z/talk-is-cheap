package springConfig.gettingStarted;

import com.alibaba.druid.pool.DruidDataSource;
import dao.gettingStarted.BlogMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:jdbc.properties")
public class SpringConfig {
    @Value("${driver}")
    private String driver;
    @Value("${jdbcUrl}")
    private String url;
    @Value("${user}")
    private String name;
    @Value("${password}")
    private String password;

    @Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(this.url);
        dataSource.setUsername(this.name);
        dataSource.setPassword(this.password);
        return dataSource;
    }

    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

//    @Bean(name = "blogMapper")
//    public BlogMapper blogMapper(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
//        MapperFactoryBean<BlogMapper> mapperFactoryBean = new MapperFactoryBean<>();
//        mapperFactoryBean.setMapperInterface(BlogMapper.class);
//        mapperFactoryBean.setSqlSessionFactory(sqlSessionFactory);
//        return mapperFactoryBean.getObject();
//    }

    @Bean(name = "mapperFactoryBean")
    public MapperFactoryBean<BlogMapper> mapperFactoryBean(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory){
        MapperFactoryBean<BlogMapper> mapperFactoryBean = new MapperFactoryBean<>();
        mapperFactoryBean.setMapperInterface(BlogMapper.class);
        mapperFactoryBean.setSqlSessionFactory(sqlSessionFactory);
        return mapperFactoryBean;
    }

    @Bean(name = "blogMapper1")
    public BlogMapper blogMapper(@Qualifier("mapperFactoryBean") MapperFactoryBean<BlogMapper> mapperFactoryBean) throws Exception {
        return mapperFactoryBean.getObject();
    }

    @Bean(name = "blogMapper2")
    public BlogMapper blogMapper(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory )  {
        SqlSessionTemplate sqlSessionTemplate=new SqlSessionTemplate(sqlSessionFactory);
        return sqlSessionTemplate.getMapper(BlogMapper.class);
    }
}
