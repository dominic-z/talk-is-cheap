package hdfs

import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/21 下午3:16
 */
class FileExists {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def fileExists():Unit={
    val path = new Path("fake_hdfs/text_file_and_dir/part*")
    val hdfs = org.apache.hadoop.fs.FileSystem.get(path.toUri,sc.hadoopConfiguration)
    println(hdfs.exists(path))// false
  }
}
