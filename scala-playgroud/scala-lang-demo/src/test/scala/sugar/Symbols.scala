package sugar

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/10 12:50 下午
 */
class Symbols {
  @Test
  def atDemo(): Unit = {
    val o: Option[Int] = Some(2)
    o match {
      case Some(x) =>
        println(x) // 2
      case None => println("test")
    }

    o match {
      case x@Some(_) =>
        println(x) //Some(2)
      case None => println("test")
    }
  }
}
