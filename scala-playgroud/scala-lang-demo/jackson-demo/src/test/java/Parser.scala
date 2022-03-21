import java.text.SimpleDateFormat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/24 下午5:32
 *//**
 *
 * @title: Parser
 * @Author Tan
 * @Date: 2020/11/24 下午5:32
 * @Version 1.0
 */
class Parser {

  private val objectMapper = new ObjectMapper

  //序列化的时候序列对象的所有属性
  objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
  //反序列化的时候如果多了其他属性,不抛出异常
  objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  //如果是空对象的时候,不抛异常
  objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
  //取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
  objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
  objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
  @Test
  def arrayToJsonStr():Unit={
    val array=Array[String]("1","2")

    println(objectMapper.writeValueAsString(array))
  }
  @Test
  def setToJsonStr():Unit={
//    看起来scala的hash和java的hash是不同的实现，因此jackson不能很好的识别
    val set=Set[String]("1","2")
    println(objectMapper.writeValueAsString(set))
  }
}
