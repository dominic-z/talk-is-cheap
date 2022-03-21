package hdfs

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/25 下午7:57
 *//**
 *
 * @title: ObjectFileDemo
 * @Author Tan
 * @Date: 2020/11/25 下午7:57
 * @Version 1.0
 */
class ObjectFileDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def saveAsObjectFile():Unit={
    val rdd=sc.parallelize(Array.range(1,10))

    rdd.saveAsObjectFile("fake_hdfs/for_obj_file/save_file")
  }

  @Test
  def readAsObjectFile():Unit={
    val rdd=sc.objectFile("fake_hdfs/for_obj_file/save_file")
  }
}
