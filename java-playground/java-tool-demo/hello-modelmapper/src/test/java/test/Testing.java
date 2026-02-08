package test;

import org.junit.Test;
import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.spi.DestinationSetter;
import org.modelmapper.spi.SourceGetter;
import org.talk.is.cheap.java.tool.demo.pojo.AccountEntity;
import org.talk.is.cheap.java.tool.demo.pojo.AddressDTO;
import org.talk.is.cheap.java.tool.demo.pojo.AddressEntity;
import org.talk.is.cheap.java.tool.demo.pojo.StudentSource;
import org.talk.is.cheap.java.tool.demo.pojo.StudentTarget;
import org.talk.is.cheap.java.tool.demo.pojo.UserDTO;
import org.talk.is.cheap.java.tool.demo.pojo.UserEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class Testing {


    @Test
    public void testMapperNestedObj() {
        // 1. 创建ModelMapper实例并配置
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT) // 严格匹配
                .setSourceNameTokenizer(NameTokenizers.CAMEL_CASE) // 支持驼峰命名拆分
                .setDestinationNameTokenizer(NameTokenizers.CAMEL_CASE);

        // 2. 构建嵌套对象和源对象
        AddressEntity addressEntity = new AddressEntity("江苏省", "南京市", "鼓楼区XX路123号");

        UserEntity userEntity = UserEntity.builder().id(1L)
                .username("zhang_san")
                .emailAddress("zhang@example.com")
                .address(addressEntity)
                .dummy("dummy")
                .accountEntityMap(Map.of(1,AccountEntity.builder().id(1).name("account1").build()))
                .build();

        // 3. 配置映射规则（处理名称不匹配的属性）
        // 3.1 配置UserEntity到UserDTO的映射
        modelMapper.typeMap(UserEntity.class, UserDTO.class)
                .addMapping(UserEntity::getId, UserDTO::setUserId) // 简单属性映射
                .addMapping(UserEntity::getEmailAddress, UserDTO::setEmail)
                .addMapping(UserEntity::getAge, UserDTO::setUserAge)
                // 嵌套对象映射：将源对象的address属性映射到目标对象的userAddress属性
                .addMapping(UserEntity::getAddress, UserDTO::setUserAddress)
                // 下列方法可以跳过dummy属性的拷贝，咋实现的，我非常好奇，看了代码也没看明白，如果只是传递一个方法，那么无论什么方式，都没法得知我在这个方法里调用了哪个类的哪个方法
                // 直到debug出真知，原来是动态生成的类
                .addMappings(mapper -> {
                    mapper.skip(new DestinationSetter<UserDTO, String>() {

                        @Override
                        public void accept(UserDTO destination, String value) {
                            // 在这debug，可以发现destination的类型是UserDTO$ByteBuddy$95BRWXvv
                            // 发现了么，这是一个在TaskDefinition中动态生成的匿名内部类，并不是原生的UserDTO，
                            // 这样的话，当调用setDummy的方法的时候，就可以通过这个动态生成的类来知道这里调用了什么方法。
                            // 比如下面的方法调用的时候，这个内部类就能够进行拦截，知道调用了setDummy，从而知道针对dummy这个property进行skip
                            //并且注意，这个value是null，是空的，只是用来作为参数传给setDummy的
                            System.out.println("dummy " + value);
                            destination.setDummy(value);
                        }
                    });

                })
        ;

        // 3.2 配置嵌套对象AddressEntity到AddressDTO的映射
        modelMapper.typeMap(AddressEntity.class, AddressDTO.class)
                .addMapping(AddressEntity::getProvince, AddressDTO::setRegion)
                .addMapping(AddressEntity::getCity, AddressDTO::setCityName)
                .addMapping(AddressEntity::getDetail, AddressDTO::setFullAddress);

        // 4. 执行映射
        UserDTO userDTO = modelMapper.map(userEntity, UserDTO.class);

        // 5. 输出结果
        System.out.println("映射结果: " + userDTO);
    }


    /**
     * 测试转换类型
     */
    @Test
    public void testConvert() {
        StudentSource userEntity = StudentSource.builder().createTime(new Date())
                .firstName("zhang")
                .lastName("san").build();

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);// 严格匹配
        // 将源对象的Date类型转换为目标对象的String类型变量
        Converter<Date, String> dateToStringConverter = new AbstractConverter<Date, String>() {
            @Override
            protected String convert(Date date) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd~~HH:mm:ss");

                return date == null ? null : simpleDateFormat.format(date);
            }
        };
        // 参考自https://blog.gitcode.com/df2c3447ca282f7aea3e4c4314ad2774.html
        // 以及https://blog.csdn.net/fly_duck/article/details/110203890
        modelMapper.typeMap(StudentSource.class, StudentTarget.class)
                .addMappings(mapper -> mapper.using(dateToStringConverter).map(StudentSource::getCreateTime,
                        StudentTarget::setCreateTimeStr))
                .addMappings(mapper -> mapper.using(new AbstractConverter<StudentSource, String>() {
                            @Override
                            protected String convert(StudentSource source) {
                                return source.getFirstName() + source.getLastName() + "哈哈";
                            }
                        }).map(source -> source, StudentTarget::setFullName)
                )
        ;
        StudentTarget target = modelMapper.map(userEntity, StudentTarget.class);
        System.out.println(target);

    }
}
