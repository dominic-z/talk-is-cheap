package api

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/20 下午2:09
 */
class OptionDemo {
  @Test
  def testOption():Unit={
    val op:Option[String]=Some("1")

    println(op.get)
  }

  @Test
  def testNone():Unit={
    val op:Option[String]=None

    println(op.get)
  }
}
