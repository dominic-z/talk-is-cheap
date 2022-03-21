import lombok.Data

/**
 * @author dominiczhu
 * @date 2020/9/19 9:22 上午
 *      scala不能很好的集成lombok注解，唯一可解决的方式就是用java代码写bean类
 *      具体讨论见
 *      https://github.com/davidB/scala-maven-plugin/issues/288
 *      https://stackoverflow.com/questions/11171631/error-compiling-java-scala-mixed-project-and-lombok
 *      因为scala的编译器工作顺序
 *      Scala goes first, since Java doesn't know anything about Scala，也就是说scala代码会被先编译
 *      然后才是java的编译器，也就是说
 *      Data这个注解需要的是java编译器
 *      但是这个类是scala编译器负责生成，所以无法使用
 */

@Data
class LombokDemo {
  private var name:String=_
}

object LombokDemo{
  def main(args: Array[String]): Unit = {
    val demo =new LombokDemo()
//    println(demo.getName())
  }
}
