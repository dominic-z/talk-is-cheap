import java.util

import com.alibaba.fastjson.{JSON, TypeReference}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2021/5/10 下午5:03
 */
/**
 *
 * @title ParserDemo
 * @author dominiczhu
 * @date 2021/5/10 下午5:03
 * @version 1.0
 */
class ParserDemo {

  def getStudent: Student = {
    val stu = new Student
    stu.setAge(10)
    stu.setName("dom")
    stu
  }

  @Test
  def ParserArray(): Unit = {
    val students = new util.ArrayList[Student]()
    students.add(getStudent)
    students.add(getStudent)

    val studentsJson = JSON.toJSONString(students)
    println(studentsJson)
    println("=========================")
    val studentArr = JSON.parseArray(studentsJson, new Student().getClass)
    println(studentArr)
    println("=========================")

    val studentList = JSON.parseObject(studentsJson, new TypeReference[util.List[Student]]() {})
    println(studentList)
    println("=========================")

//    下列代码无法编译通过，说到底还是类的实现方式不同，parserArray返回的是util.List[_<:Student]
//    println("=========================")
//    val errorWhenCompile:util.List[Student] = JSON.parseArray(studentsJson, new Student().getClass)
//    println(errorWhenCompile)
  }
}
