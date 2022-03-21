package hdfs

import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/21 下午3:43
 */
class PathDemo {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def pathDemo():Unit={
    val path = new Path("hdfs://aaa/user")
    println(s"path.toString:${path.toString}")
    println(s"path.toUri:${path.toUri}")
    println(s"path.getName:${path.getName}")
    println(s"path.getParent:${path.getParent}")
    println(s"path.isRoot:${path.isRoot}")
    println(s"path.isAbsolute:${path.isAbsolute}")
  }
}
