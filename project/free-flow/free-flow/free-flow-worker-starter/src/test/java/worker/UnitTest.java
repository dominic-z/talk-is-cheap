package worker;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.StageDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.message.impl.dto.TaskDefinitionDTO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.JsonInputCodec;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

@Slf4j
public class UnitTest {


    static class Parent<T> {

    }

    static class Child<T> extends Parent<T> {
        boolean classEqual(Class<?> clazz) {
            Type genericSuperclass = this.getClass().getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                // 获取父类的泛型参数（此处为User.class）
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length > 0) {
                    Type type = actualTypeArguments[0];
                    System.out.println("父类的泛型参数类型：" + type.getTypeName() + "??" + type.getClass()); // 输出：User

                }
            }
            return true;
        }
    }


    @Test
    public void testCodec() throws Exception {
        System.out.println(getObj(StageDefinitionDTO.class));

    }

    private <T> T getObj(Class<T> tClass) throws Exception {
        String s = """
                {
                  "id": 0,
                  "taskId": 0,
                  "stageName": "stageName_3bdc79d29f00",
                  "version": 0,
                  "stageType": 0,
                  "isStartingStage": false,
                  "timeout": 0,
                  "maxRetryCount": 0
                }
                """;

        Constructor<JsonInputCodec> constructor = JsonInputCodec.class.getConstructor();
        JsonInputCodec jsonInputCodec = constructor.newInstance();

        Object decode = jsonInputCodec.decode(s, tClass);
        return (T) decode;
    }

    @Test
    public void testModelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        final AbstractConverter<Class<?>, String> classNameConverter = new AbstractConverter<>() {
            @Override
            protected String convert(Class<?> source) {
                return source.getName();
            }
        };
        modelMapper.typeMap(TaskDefinitionBO.class, TaskDefinitionDTO.class)
                .addMappings(mapper -> {
                    mapper.using(classNameConverter)
                            .map(TaskDefinitionBO::getSharedContextClass, TaskDefinitionDTO::setSharedContextFullyQualifiedClassName);
                    mapper.using(classNameConverter)
                            .map(TaskDefinitionBO::getSharedContextCodecClass,
                                    TaskDefinitionDTO::setSharedContextCodecFullyQualifiedClassName);
                });

        modelMapper.typeMap(StageDefinitionBO.class, StageDefinitionDTO.class)
                .addMappings(mapper -> {
                    mapper.using(classNameConverter)
                            .map(StageDefinitionBO::getInputClass, StageDefinitionDTO::setInputFullyQualifiedClassName);
                    mapper.using(classNameConverter)
                            .map(StageDefinitionBO::getInputCodecClass, StageDefinitionDTO::setInputCodecFullyQualifiedClassName);
                });


        StageDefinitionBO stage = StageDefinitionBO.builder()
                .name("stage")
                .timeout(3)
                .maxRetryCount(3).build();
        TaskDefinitionBO taskDefinitionBO = TaskDefinitionBO.builder()
                .name("aa")
                .sharedContextClass(Object.class)
                .sharedContextCodecClass(JsonInputCodec.class)
                .roots(Set.of("1", "2"))
                .maxRetryCount(3)
                .pointOutGraph(Map.of("a", Set.of("bnb", "bn")))
                .stageDefinitionBOMap(Map.of("stage", stage)).build();

        TaskDefinitionDTO taskDefinitionDTO = modelMapper.map(taskDefinitionBO, TaskDefinitionDTO.class);
        
        log.info("{}",taskDefinitionDTO);
    }

}
