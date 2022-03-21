package reflection
import org.junit.Test

import scala.reflect.runtime.{universe=>ru}
/**
 * @author dominiczhu
 * @date 2021/7/1 下午2:44
 */
/**
 *
 * @title Demo
 * @author dominiczhu
 * @date 2021/7/1 下午2:44
 * @version 1.0
 */
class Demo {

  @Test
  def testBounds():Unit={
    val l = List(1,2,3)
    def getTypeTag[T: ru.TypeTag](obj: T) = {
      ru.typeTag[T]
    }
    val theType = getTypeTag(l).tpe
    println(theType)

    val decls = theType.decls.take(10)
    println(decls)
  }

}
