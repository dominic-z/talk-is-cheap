package functionsDemo

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/9/19 4:59 下午
 */
class WindowFunctionDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
  import spark.implicits._

  @Test
  def rowNumberDemo()={
    val df = spark.read.json("employeesForUdf.json")
    val rowNumber=row_number().over(Window.partitionBy($"gender").orderBy(asc("salary")))
    df.withColumn("row_number",rowNumber).show()
  }

}
