package objectFactory;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.List;
import java.util.Properties;

public class ExampleObjectFactory extends DefaultObjectFactory {
    String someProperty;

    @Override
    public <T> T create(Class<T> type) {
        System.out.println("创建对象类型为"+type);
        return super.create(type);
    }

    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
//        创建对象
        System.out.println("创建对象类型为"+type);
        return super.create(type, constructorArgTypes, constructorArgs);
    }

    @Override
    public <T> boolean isCollection(Class<T> type) {
        System.out.println("判断是否是容器类型");
        return super.isCollection(type);
    }

    @Override
    public void setProperties(Properties properties) {
//        设置属性
        this.someProperty=properties.getProperty("someProperty");
        System.out.println(this.someProperty);
    }
}
