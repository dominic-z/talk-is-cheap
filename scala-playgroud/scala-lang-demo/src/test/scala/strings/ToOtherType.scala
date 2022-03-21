package strings

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/12/17 下午4:51
 *//**
 *
 * @title: ToOtherType
 * @Author Tan
 * @Date: 2020/12/17 下午4:51
 * @Version 1.0
 */
class ToOtherType {

  @Test
  def toBoolean():Unit={
    val s1="true"
    println(s1.toBoolean)
    val s2="true1"
    println(s2.toBooleanOption)
    val s3="True"
    println(s3.toBooleanOption)
    val s4="True1"
    println(s4.toBoolean)
  }

}
