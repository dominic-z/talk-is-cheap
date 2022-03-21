/**
 * @author dominiczhu
 * @date 2020/9/25 7:41 下午
 */
package object pkgObj {
  class Animal(val id:Int){
    println(s"animal$id construct")
  }

  val animal=new Animal(1)
  val animal2=new Animal(2)

}
