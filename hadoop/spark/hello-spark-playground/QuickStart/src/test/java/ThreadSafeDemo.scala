import java.text.SimpleDateFormat

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2021/5/21 下午4:42
 */
/**
 *
 * @title ThreadSafeDemo
 * @author dominiczhu
 * @date 2021/5/21 下午4:42
 * @version 1.0
 */
class ThreadSafeDemo {

  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[3]")
  val sc = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  def dateDemoRdd: RDD[String] = {
    val arr = Array.range(0, 100).map(_=>"20210530")
    sc.parallelize(arr, 3)
  }

  @Test
  def simpleDateFormatTest(): Unit = {
    val demoRdd = dateDemoRdd
    val dateFormat = new SimpleDateFormat("yyyyMMdd")

    val res = demoRdd.map(s => dateFormat.parse(s)).collect()
    println(res.toSeq)

  }

}
