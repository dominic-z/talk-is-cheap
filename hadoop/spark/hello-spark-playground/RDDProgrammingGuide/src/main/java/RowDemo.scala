import java.util

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * @author dominiczhu
 * @date 2020/8/31 8:13 下午
 */
object RowDemo {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._
  import org.apache.spark.sql._

  def main(args: Array[String]): Unit = {
    simpleAgg
  }

  def simpleAgg(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val df = spark.read.json("employees.json")
    df.createOrReplaceTempView("employees")
    val pairs = spark.sql("SELECT name, salary FROM employees").rdd.map {
      case Row(name: String, salary: Int) =>
        name -> salary
    }
    println(pairs.collect().mkString(" "))
  }
}
