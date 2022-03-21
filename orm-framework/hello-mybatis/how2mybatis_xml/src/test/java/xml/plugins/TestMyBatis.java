package xml.plugins;

import dao.AuthorMapper;
import dao.BlogMapper;
import domain.Author;
import domain.Blog;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;

public class TestMyBatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("xml/plugins/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);
    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
            BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

            Author author=authorMapper.selectById(1);
            System.out.println(author);

            Blog blog=blogMapper.selectById(1);
            System.out.println(blog);
        }
        /*打印结果
        插件配置信息：{someProperty=100}
someProperty：100

ExamplePlugin...plugin:org.apache.ibatis.executor.CachingExecutor@7f010382
ExamplePlugin...plugin:org.apache.ibatis.scripting.defaults.DefaultParameterHandler@49d904ec
ExamplePlugin...plugin:org.apache.ibatis.executor.resultset.DefaultResultSetHandler@418e7838
ExamplePlugin...plugin:org.apache.ibatis.executor.statement.RoutingStatementHandler@3c130745
ExamplePlugin...intercept:public abstract java.sql.Statement org.apache.ibatis.executor.statement.StatementHandler.prepare(java.sql.Connection,java.lang.Integer) throws java.sql.SQLException
当前拦截到的对象：org.apache.ibatis.executor.statement.RoutingStatementHandler@3c130745
SQL语句：select * from author where id = ?
SQL语句入参：1
SQL语句类型：SELECT
Mapper方法全路径名：dao.AuthorMapper.selectById
修改后SQL语句：select * from author where id = ? limit 2
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
ExamplePlugin...plugin:org.apache.ibatis.scripting.defaults.DefaultParameterHandler@7d8995e
ExamplePlugin...plugin:org.apache.ibatis.executor.resultset.DefaultResultSetHandler@130d63be
ExamplePlugin...plugin:org.apache.ibatis.executor.statement.RoutingStatementHandler@42a48628
ExamplePlugin...intercept:public abstract java.sql.Statement org.apache.ibatis.executor.statement.StatementHandler.prepare(java.sql.Connection,java.lang.Integer) throws java.sql.SQLException
当前拦截到的对象：org.apache.ibatis.executor.statement.RoutingStatementHandler@42a48628
SQL语句：select * from blog where id = ?
SQL语句入参：1
SQL语句类型：SELECT
Mapper方法全路径名：dao.BlogMapper.selectById
修改后SQL语句：select * from blog where id = ? limit 2
Blog{id=1, content='content11', authorId=1, authorName='zhang'}
        */
    }
}
