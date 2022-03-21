package collections

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/2 7:04 下午
 */
class Immutable {
  @Test
  def setDemo():Unit={
    val s1=Set("a")
    val s2=Set("a","b")
    println(s1.concat(s2))
    println(s1.union(s2))
  }
}
