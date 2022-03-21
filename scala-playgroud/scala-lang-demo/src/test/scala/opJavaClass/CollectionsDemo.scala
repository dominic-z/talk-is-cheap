package opJavaClass

import java.util

import org.junit.Test


/**
 * @author dominiczhu
 * @date 2020/11/20 下午2:16
 */
class CollectionsDemo {
  @Test
  def sendAList():Unit={
    val j=new JavaCollectionsDemo

    val stringList=new util.ArrayList[String]()
    stringList.add("a")
    j.consumeList(stringList)
  }
}
