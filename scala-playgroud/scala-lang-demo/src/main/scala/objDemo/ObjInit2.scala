package objDemo

/**
 * @author dominiczhu
 * @date 2020/11/6 1:03 下午
 */
object ObjInit2 {
  val c2=new C2

  def main(args: Array[String]): Unit = {
    println(c2)

//    调用单例对象的时候，才会构建这个Object对象，构建Object对象的时候，会同时把其持有的对象都构建出来。
//    println(ObjInit)
    ObjInit.show()
  }
}
