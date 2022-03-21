package datasetDemo

import cases.{Person, Student, StudentScore}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StringType
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2021/5/13 下午7:24
 */
/**
 *
 * @title DsMetaInfo
 * @author dominiczhu
 * @date 2021/5/13 下午7:24
 * @version 1.0
 */
class DsMetaInfo {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._


  @Test
  def dsSchemaDemo(): Unit = {
    /*
    * 类型不同也可以join
    * */
    val ds = Seq(Person("32", 1), Person("12", 3)).toDS()
    println("============show schema==============")
    val schema = ds.schema
    println(schema)

    println("============show fields==============")
    println(schema.fields.toSeq)
    println(schema.fields(0).dataType == StringType)

    schema.foreach(struct => println(struct.name))
  }


  @Test
  def casesDfJoin(): Unit = {


    val leftDS = Seq(StudentScore(Student("mike", 19), 89), StudentScore(Student("amy", 18), 85)).toDS()
    val rightDS = Seq(StudentScore(Student("mike", 19), 89), StudentScore(Student("tom", 20), 87)).toDS()

    leftDS.show()
    rightDS.show()
    val resDf = leftDS.join(rightDS, Seq("student", "score"))
    resDf.show()
  }

}
