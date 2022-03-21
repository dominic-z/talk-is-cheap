import org.apache.commons.lang.StringUtils
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/2 8:27 下午
 */
class basic {
  @Test
  def forYield():Unit={
    case class Book(title : String , authors : String* )  //声明书本类

    val books :List[Book] =       //建立测试数据
      List(
        Book(
          "Structure and Interpretation of Computer Programs",
          "Abelson,Harold","Sussman,Gerald J."
        ),Book(
          "Principles of Compiler Design",
          "Aho,Alfred","Ullman,Jeffrey"
        ),Book(
          "Elements of ML Programming",
          "Ullman,Jeffrey"
        ),Book(
          "The Java Language Specification","Gosling,James",
          "Joy,Bill","Steele,Guy","Bracha,Gilad"
        )
      )
    //寻找姓Ullman的作者 这个if是对最内层起作用
    val title=for(b <- books ;a:String <- b.authors ;result=b.title ;if a.startsWith("Ullman"))
      yield result

    println(title.getClass)
    println(title)

  }
  @Test
  def testError1():Unit={
    val s=",adfs"
//    测试空行错误，这个报错信息和2.11有差别
    println(StringUtils.split(s,",").head)
  }
}
