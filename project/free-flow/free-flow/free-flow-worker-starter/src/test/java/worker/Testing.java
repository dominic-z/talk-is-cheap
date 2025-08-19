package worker;


import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.DestinationSetter;
import org.talk.is.cheap.project.free.flow.common.message.impl.QueryTaskDefinitionResp;
import org.talk.is.cheap.project.free.flow.common.message.impl.vo.TaskDefinitionVO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.StageDefinitionBO;
import org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO;
import org.talk.is.cheap.project.free.flow.starter.worker.task.definition.annotaion.stage.RunnableStage;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

public class Testing {


    static class Parent<T>{

    }

    static class Child<T> extends Parent<T>{
        boolean classEqual(Class<?> clazz){
            Type genericSuperclass = this.getClass().getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType){
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
                // 获取父类的泛型参数（此处为User.class）
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

                if (actualTypeArguments.length > 0) {
                    Type type = actualTypeArguments[0];
                    System.out.println("父类的泛型参数类型：" + type.getTypeName()+"??"+type.getClass()); // 输出：User

                }
            }
            return true;
        }
    }


    static class T{
        @RunnableStage(name = "stage",version = 1)
        public void stage(){

        }
    }
    @Test
    public void test(){
//        System.out.println(CaseFormat.LOWER_HYPHEN.converterTo(CaseFormat.LOWER_CAMEL).convert("connect-url"));
//        new CuratorConfig().zkConfigProperties();

//        System.out.println(Paths.get("/aa","fdsfds"));

//        System.out.println(new Child<String>().classEqual(String.class));

//        System.out.println(Object.class.toString());
//        System.out.println(T.class.getMethods()[0].getAnnotation(RunnableStage.class).annotationType());


    }


    /**
     * 发现了个好玩的modelMapper，可以用来深度拷贝，甚至还能跳过一些对象
     */
    @Test
    public void testModelMapper(){

        System.out.println(QueryTaskDefinitionResp.QueryTaskDefinitionRespData.class);
        TaskDefinitionBO taskDefinitionBO = new TaskDefinitionBO();
        taskDefinitionBO.setName("aaa");


//        Child.class.getDeclaredMethods()[0].getParameters()

        StageDefinitionBO stageDefinitionBO = StageDefinitionBO.builder().toStageNames(Set.of("test1", "test2")).fromStageNames(Set.of(""))
                .parameters(Child.class.getDeclaredMethods()[0].getParameters())
                .build();
        taskDefinitionBO.getStageDefinitionBOMap().put("a", stageDefinitionBO);

        ModelMapper modelMapper = new ModelMapper();

        // 下列方法可以跳过Name属性的拷贝，咋实现的，我非常好奇，看了代码也没看明白，如果只是传递一个方法，那么无论什么方式，都没法得知我在这个方法里调用了哪个类的哪个方法
        // 直到debug出真知，原来是动态生成的类
        modelMapper.typeMap(TaskDefinitionBO.class,TaskDefinitionBO.class)
                .addMappings(mapper->{
                    mapper.skip(new DestinationSetter<TaskDefinitionBO, String>() {

                        @Override
                        public void accept(TaskDefinitionBO destination, String value) {
                            // 在这debug，可以发现destination的类型是org.talk.is.cheap.project.free.flow.common.task.definition.bo.TaskDefinitionBO$ByteBuddy$95BRWXvv
                            // 发现了么，这是一个在TaskDefinition中动态生成的匿名内部类，并不是原生的TaskDefinitionBO，这样的话，当调用setName的时候，就可以通过这个动态生成的类来知道这个方法里调用了什么。
                            // 比如下面的方法调用的时候，这个内部类就能够进行拦截，知道调用了setName，从而知道针对name这个property进行skip
                            destination.setName(value);
                        }
                    });
                });

//        https://modelmapper.org/user-manual/faq/
//        Is ModelMapper threadsafe?
//Yes

        TaskDefinitionBO outputBO = modelMapper.map(taskDefinitionBO, TaskDefinitionBO.class);
        taskDefinitionBO.getStageDefinitionBOMap().put("b",new StageDefinitionBO());
        System.out.println(outputBO);


        TaskDefinitionVO outputVO = modelMapper.map(taskDefinitionBO, TaskDefinitionVO.class);
        System.out.println(outputVO);
    }
}
