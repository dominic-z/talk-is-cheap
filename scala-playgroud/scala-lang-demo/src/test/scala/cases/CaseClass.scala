package cases

import org.junit.Test

/**
 * @author dominiczhu
 * @date 2021/8/24 下午7:12
 */
/**
 *
 * @title CaseClass
 * @author dominiczhu
 * @date 2021/8/24 下午7:12
 * @version 1.0
 */
class CaseClass {

  @Test
  def copy(): Unit = {
    val habit1 = Habit(mainHabit = "play", habits = Array("eat"))
    val habit2 = Habit(mainHabit = "swim", habits = Array("eat", "get fat"))

    val stu1 = Student(name = "zxy", age = 12, habit = habit1)
    val copyStu1 = stu1.copy(age = 18)

    // copy为浅拷贝
    println(stu1 eq copyStu1)
    println(stu1.habit eq copyStu1.habit)
    println(stu1.name eq copyStu1.name)
    stu1.name = "lxy"
    println(stu1.name eq copyStu1.name)
  }

  @Test
  def caseObj():Unit={
    println(Habit.objFunc())
  }

  @Test
  def overwriteApply():Unit={
    println(Habit("p"))
  }

}
