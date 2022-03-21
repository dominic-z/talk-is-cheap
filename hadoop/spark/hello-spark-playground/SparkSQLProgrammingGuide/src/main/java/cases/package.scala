import domain.enums.GENDER

/**
 * @author dominiczhu
 * @date 2020/8/27 10:03 上午
 */
package object cases {

  case class Student(stuName: String, stuAge: Int)

  case class Person(name: String, age: Long)

  case class StudentScore(student: Student, score: Int)

  case class Employer(name: String, city: String, sex: Int, salary: Double)

  case class Person2(name: String, gender: GENDER)

}
