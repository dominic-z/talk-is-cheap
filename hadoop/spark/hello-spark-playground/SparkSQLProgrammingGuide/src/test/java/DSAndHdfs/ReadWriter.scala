package DSAndHdfs

import cases.Person
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/21 下午7:48
 */
class ReadWriter {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
  import spark.implicits._
  @Test
  def json(): Unit = {
    val ds = Seq(Person("Andy", 32), Person("Mike", 32)).toDS()
    ds.write.json("data/output/output.json")
  }

  @Test
  def csv(): Unit = {
    val ds = Seq(Person("Andy", 32), Person("Mike", 32)).toDS()
    ds.write.csv("data/output/output_csv")
  }

  @Test
  def nullCsv(): Unit = {
    val ds = Seq(Person("Andy", 32), Person(null, 32)).toDS()
    ds.write.csv("data/output/null_output_csv")
  }

  @Test
  def readCsv(): Unit = {
//    val ds = Seq(Person("Andy", 32), Person("Mike", 32)).toDS()
    val ds=spark.read.csv("data/output/output_csv")
    ds.show()
  }


  @Test
  def readNullCsv(): Unit = {
    //    val ds = Seq(Person("Andy", 32), Person("Mike", 32)).toDS()
    val ds=spark.read.csv("data/output/null_output_csv")
    ds.collect().foreach(row=>println(row.getString(0))) // 通过csv读取，""代表的是null

    val rdd=sc.textFile("data/output/null_output_csv") // 通过sc读取，""原样读取
    rdd.collect().foreach(println)
  }
}
