package cases

/**
 * @author dominiczhu
 * @date 2021/8/24 下午7:11
 */
case class Habit(mainHabit: String, habits: Array[String]){
  override def toString: String = s"${this.getClass}(${mainHabit},${habits.toSeq})"
}


// just object also do the job
case object Habit{
  def objFunc(): Habit ={
    Habit("play",Array("play"))
  }

  def apply(mainHabit: String, habits: Array[String]): Habit = new Habit(mainHabit, habits)

  def apply(s:String):Habit={
    Habit(s,Array())
  }
}