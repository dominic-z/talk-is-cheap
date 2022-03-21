package hdfs

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/12/11 上午9:26
 *//**
 *
 * @title: FileInfoDemo
 * @Author Tan
 * @Date: 2020/12/11 上午9:26
 * @Version 1.0
 */
class FileInfoDemo {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()
  val hdfs = FileSystem.get(sc.hadoopConfiguration)

  @Test
  def dirInfo():Unit={
    val dir = new Path("fake_hdfs/text_file_and_dir")
    println(s"length: ${hdfs.getContentSummary(dir).getLength}")
  }
}
