package function

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/9/17 2:38 下午
 */
class Demo {
  class LazyOperate{
    // 模拟一个懒加载对象，setFunction吃掉一个函数，然后exec的时候执行
    var f:Int=>Int=_
    def setFunction(f:(Int=>Int)):Unit={
      this.f=f
    }
    def exec():Int={
      f(1)
    }
  }
  @Test
  def highLevelFuncAndVar():Unit={
    var arr:Array[LazyOperate]=Array()
    for(i<-Array.range(1,10)){
      val l=new LazyOperate()
      val a=i
      // 传入的方法使用了循环之中的val a
      l.setFunction(_=>a)
      arr=arr:+l
    }
    for(l<-arr)
      println(l.exec())

    var i=10
    val l=new LazyOperate()
    // 传入的函数使用了外部的var i
    l.setFunction(_=>i)
    println(l.exec())
    i=20
    println(l.exec())
    // 可以看到结果变成了20，也就是说scala的函数在执行的时候，如果引用了外部变量，外部变量发生改变的时候，执行结果也会不同
    // 一定要注意这件事
    // 在java里，高阶函数的传入函数如果引用了外部变量，外部变量也最好是final的
  }
}
