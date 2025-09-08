package org.talk.is.cheap.project.free.flow.common.utils;

import cn.hutool.core.text.escape.Html4Escape;
import lombok.extern.slf4j.Slf4j;
import org.talk.is.cheap.project.free.flow.common.exception.IllegalTaskDefinitionException;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.InputCodec;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * 反射工具
 */
@Slf4j
public class ReflectUtil {


    /**
     * 使用反射，根据InputCodec的类对象获取泛型类型，从而获取输入参数类型的真实类型
     *
     * @param clazz
     * @return
     */
    public static Class<?> getCodecGenericClass(Class<? extends InputCodec<?>> clazz) throws IllegalTaskDefinitionException {


        Class<?> c = clazz;
        while (InputCodec.class.isAssignableFrom(c)) {
            Type superclassType = c.getGenericSuperclass();
            if (superclassType instanceof ParameterizedType parameterizedType) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                // 输出结果
                for (Type type : actualTypeArguments) {
                    // 判断是否为具体类型（Class 实例）
                    if (type instanceof Class) {
                        log.info("found specific generic class in codec: {}", type);
                        return (Class<?>) type;
                    }
                    // 判断是否为单纯的泛型变量（TypeVariable 实例）
                    else if (type instanceof TypeVariable) {
                        log.error("The generic type of the codec must be a concrete type, not a generic class.: {}", clazz);
                        throw new IllegalTaskDefinitionException("The generic type of the codec must be a concrete type, not a " +
                                "generic class.");
                    }
                    // 其他情况（如嵌套泛型、通配符等）
                    else {
                        log.error("No specific generic class can be found in the codec.{}", clazz);
                        throw new IllegalTaskDefinitionException("No specific generic class can be found in the codec.");
                    }
                }
            }
            c = c.getSuperclass();
        }

        log.error("No specific generic class can be found in the codec.{}", clazz);
        throw new IllegalTaskDefinitionException("No specific generic class can be found in the codec.");


    }

}
