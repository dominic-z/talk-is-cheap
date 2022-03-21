package collections

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/24 下午12:32
 *//**
 *
 * @title: Mutable
 * @Author Tan
 * @Date: 2020/11/24 下午12:32
 * @Version 1.0
 */
class Mutable {
  case class Case(name:String,age:Int)
  @Test
  def testSet():Unit={
    val s=Set(Case("a",2))
    println(s)
  }

  @Test
  def testTupleSet():Unit={
    val s=Set(("a","b"),("c","d"),("c","d"))
    println(s)
  }

  @Test
  def testArrayDistinct():Unit={
    val s=Array(("a","b"),("c","d"),("c","d"))
    println(s.distinct.toSeq)
  }

  @Test
  def sortArray():Unit={
    val s=Array("aa/20201130/202011300100","aa/20201130/202011300130","aa/20201130/202011300000")
    println(s.sorted.toSeq.reverse)

  }

  @Test
  def intersect():Unit={
    val arr1="aaabc".toArray
    val arr2="aac".toArray
    println(arr1.intersect(arr2).toSeq)
  }
}
