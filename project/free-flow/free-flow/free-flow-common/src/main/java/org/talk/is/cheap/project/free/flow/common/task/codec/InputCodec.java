package org.talk.is.cheap.project.free.flow.common.task.codec;

import org.apache.commons.lang3.NotImplementedException;


/**
 * 碎碎念：
 * 需求：
 * 1. 在stage运行过程中，外界或者上一个stage传入的是编码后的input对象（例如str），需要对其解码还原成对应的input对象（具体是什么类型是不确定的）
 * <p>
 * 这需要：知道input的真实类型信息，知道编码和解码的方法。
 * <p>
 * 方案1：
 * 创建StageInput<T>抽象父类，所有input都需要实现这个抽象父类，并让input对象自己提供编解码方法。
 * 结果：
 * 开发者需要自己实现编码解码，还要自己处理getter、setter等方法，这根本没有提供强类型，仍然是让开发者自己实现类型转换
 * 因此，我们需要的是一个编码解码器
 * <p>
 * 方案2：
 * 创建统一的InputCodec<T>编解码器对象，T作为泛型指代intput类型，并提供JsonInputCodec<T> extends InputCodec<T>等默认编解码器。在stage的定义中指定对应的编解码器
 * 结果：
 * 功能倒是能实现了，但是，在获取真实类型信息的时候会很麻烦。
 * 多数情况下我们会使用Json作为序列化方案，那定义一个JsonInputCodec<T>类型后，在真实使用的时候用的是JsonInputCodec<SomeObj>，运行时也没有任何办法获取真正的SomeObj.class，
 * 也就是说无法从这个JsonInputCodec<SomeObj>的身上获取SomeClass.class
 * 真实的类型只能从method的方法定义里找。
 * 但是这带来一个问题是，无法知道指定的inputCodec和inputClass是否能够对应，
 * 如果希望能够前置完成判断，必须在InputCodec再增加一个方法判断是否能够处理某个inputClass，
 * 而这反过来又需要：InputCodec得能拿到真实的Class。例如可以通过构造方法传入；更麻烦了
 * 能不能只指定一个Codec呢？
 * 能，继承关系的时候，父类的泛型信息会被保留。用的时候只要public class MyCoded extend JsonInputCodec<SomeClass>就好了
 * 
 *
 *
 * 20250904：由接口改为了抽象类，这是因为
 * 1. 我需要通过反射获取实际的泛型类型，如果是抽象类，那么只能是继承关系，只需要getGenericSuperClass即可，如果设计为接口，那么就复杂得多，需要通过getGenericInterfaces和getGenericSuperclass寻找所有的泛型父类，找到对应的inputCodec
 * 2. 避免多实现，避免一个InputCodec的实现类拥有多个重载的encode和decode
 * @param <T>
 */
public abstract class InputCodec<T> {


    abstract public String encode(T t);

    abstract public T decode(String encode, Class<T> tClass);

}
