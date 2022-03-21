package others

import java.io.{ByteArrayInputStream, StringBufferInputStream, StringReader}
import java.util.Properties

import org.junit.Test

import collection.mutable
/**
 * @author dominiczhu
 * @date 2020/11/17 下午8:17
 */
class OtherDemo {

  @Test
  def demo1(): Unit ={
    val s="a,b,c,a,c"
    println(Set(s.split(",")))
  }

  @Test
  def arrayPropertyDemos():Unit={
    val params=Array("a=1","b=2","c=10")
    val props=new Properties()
    props.load(new StringReader(params.mkString("\n")))
    println(props)
    println(props.getProperty("c"))
    println(props.getProperty("d"))
  }
}
