import org.junit.Test

import scala.util.Random

/**
 * @author dominiczhu
 * @date 2020/8/28 3:02 下午
 */
class Demo {

  @Test
  def demo(): Unit = {
    println(Math.abs(Random.nextInt()))
  }
  def func():(Boolean,Int)={
    (true,1)
  }

}
