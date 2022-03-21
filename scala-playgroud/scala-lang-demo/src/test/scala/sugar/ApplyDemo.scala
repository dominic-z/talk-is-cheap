package sugar

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/10 12:51 下午
 */
class ApplyDemo {
  @Test
  def applyDemo() = {
    class CanApply {
      def apply(a: String): String = {
        "123"
      }
    }
    val canApply=new CanApply
    println(canApply("13"))
  }
}
