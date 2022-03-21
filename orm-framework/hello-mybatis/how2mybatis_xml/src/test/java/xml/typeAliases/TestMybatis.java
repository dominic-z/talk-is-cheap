package xml.typeAliases;

import dao.AuthorMapper;
import domain.Author;
import domain.Sex;
import domain.SexDetail;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;
import wiredDomain.AuthorName;

import java.io.IOException;
import java.io.Reader;

public class TestMybatis {
    SqlSessionFactoryBuilder sqlSessionFactoryBuilder=new SqlSessionFactoryBuilder();
    Reader resource;

    {
        try {
            resource = Resources.getResourceAsReader("xml/typeAliases/mybatis-config.xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBuilder.build(resource);
    @Test
    void test1(){
        try(SqlSession sqlSession=sqlSessionFactory.openSession()){
            AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);

            AuthorName authorName=authorMapper.selectAuthorNameById(1);
            System.out.println(authorName);

            Author author=authorMapper.selectByAuthorNameObj(authorName);
            System.out.println(author);

//            演示EnumTypeHandler
            Sex sex=authorMapper.selectAuthorSexById(1);
            System.out.println(sex);
//          演示EnumOrdinalTypeHandler
            SexDetail sexDetail=authorMapper.selectAuthorSexByIdWithIndex(1);
            System.out.println(sexDetail);
        }
    }
    /*结果
    * 通过列名调用getNullableResult
我是：AuthorName{name='zhang'}
调用setNonNullParameter方法
Author{id=1, name='zhang', age=18.5, sexType=0, sex='MALE'}
MALE
SexDetail{i=0, gender='male'}*/
}
