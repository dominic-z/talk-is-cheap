package typeHandlers;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import wiredDomain.AuthorName;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.VARCHAR)//指定数据库内格式
//@MappedTypes(AuthorName.class)//这个是指定javatype，不过我觉得有明确的泛型了就可以不写，个人猜测是用在处理多种类型的时候用到该注解
// 如public class AuthorNameHandler extends BaseTypeHandler<T extends Animal>，然后T需要是cat，dog之类的子类
//上述两个注解的优先级低于xml配置文件中的<typeHandler handler="typeHandlers.AuthorNameHandler" jdbcType="VARCHAR" javaType="wiredDomain.AuthorName"/>jdbcType和javaType属性
public class AuthorNameHandler extends BaseTypeHandler<AuthorName> {//泛型确定java对象类型
//从而实现执行sql和结果处理过程中，JdbcType.VARCHAR和AuthorName的类型转换
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, AuthorName authorName, JdbcType jdbcType) throws SQLException {
        System.out.println("调用setNonNullParameter方法");
        /*如果进行带参sql时，如果java方法的传入参数的为Author，如何将其转化为sql参数*/
        preparedStatement.setString(i, authorName.getName());
    }

    @Override
    public AuthorName getNullableResult(ResultSet resultSet, String s) throws SQLException {
        System.out.println("通过列名调用getNullableResult");
        /*从结果中抽取数据时，如果需要的类型是AuthorName，而获取的结果是varchar类型的，如何进行转换
        * 因为处理的jdbctype是varchar类型的，因此获取列数据的时候选择使用的是getString */
        String name=resultSet.getString(s);
        AuthorName authorName=new AuthorName();
        authorName.setName(name);
        return authorName;
    }

    @Override
    public AuthorName getNullableResult(ResultSet resultSet, int i) throws SQLException {
        System.out.println("通过列序号调用getNullableResult");
        /*功能同上，只不过上面是通过列名获取，这个是通过列序号获取*/
        String name=resultSet.getString(i);
        AuthorName authorName=new AuthorName();
        authorName.setName(name);
        return authorName;
    }

    @Override
    public AuthorName getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        System.out.println("通过存储过程调用getNullableResult");
        //这个方法用在存储过程中
        String name=callableStatement.getString(i);
        AuthorName authorName=new AuthorName();
        authorName.setName(name);
        return authorName;
    }
}
