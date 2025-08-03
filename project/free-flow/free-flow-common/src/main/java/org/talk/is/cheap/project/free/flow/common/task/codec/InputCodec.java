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
 * 创建StageInput抽象父类，所有input都需要实现这个抽象父类，并让input对象自己提供编解码方法。
 * 结果：
 * 失败，因为每个stage的input是什么本身不重要，重要的是如何对其序列化成str以及反序列化回类型，需要的是一个额外编解码器，而不需要限定input的类型是什么
 * <p>
 * 方案2：
 * 创建统一的InputCodec<T>编解码器对象，T作为泛型指代intput类型，并提供JsonInputCodec<T> extends InputCodec<T>等默认编解码器。在stage的定义中指定对应的编解码器
 * 结果：
 * 功能倒是能实现了，但是，在获取真实类型信息的时候会很麻烦。
 * 多数情况下我们会使用Json作为序列化方案，那定义一个JsonInputCodec<T>类型后，在真实使用的时候用的是JsonInputCodec<SomeObj>，运行时也没有任何办法获取真正的SomeObj.class，也就是说T的真实类型信息丢失了
 * 在反序列化成input对象的时候，因为类型信息丢失了，无法进行类型强转，因此还Stage需要一个额外的参数tClass来执行input的class类型，例如
 * Class<?> inputClass()default String.class;
 * Class<?extends InputCodec<?>> inputCodec()default SimpleStringInputCodec.class;
 * 然后在stage运行的时候这样操作inputClass.cast(ctx.inputCodec.decode(inputStr))。实现是实现了，但是指定两个确实参数有点麻烦，而且
 * 另一个问题是：因为注解不能用泛型，无法知道指定的inputCodec和inputClass是否能够对应，如果希望能够前置完成判断，必须在InputCodec再增加一个方法判断是否能够处理某个inputClass，这需要：InputCodec需要持有一个泛型的Class
 * <T>对象，例如可以通过构造方法传入；更麻烦了
 * 能不能只指定一个Codec呢？
 * <p>
 * <p>
 * <p>
 * 仔细回想在stage中的使用流：输入string类型的序列化后的对象 --> 解码并在stage运行的时候强制类型转换回输入对象  --> 使用对象 -->  创建下一个stage的input对象 --> 序列化后并塞入上下文
 * 那么其实不需要持有tClass啊，在stage里自己就知道这个Class是啥啊。。。自己在stage代码里转就好了。。
 *
 * @param <T>
 */
public interface InputCodec<T> {


    String encode(T t);

    T decode(String encode, Class<T> tClass);

}
