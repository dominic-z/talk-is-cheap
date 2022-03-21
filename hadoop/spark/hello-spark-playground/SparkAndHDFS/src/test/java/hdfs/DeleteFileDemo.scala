package hdfs

import org.apache.hadoop.fs.Path
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @author dominiczhu
 * @date 2020/11/17 下午7:17
 */
class DeleteFileDemo {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  def deleteHdfsFile(): Unit = {
    val path = new Path("hdfs://xxx/xxx")
    val hdfs = org.apache.hadoop.fs.FileSystem.get(path.toUri,sc.hadoopConfiguration)
    if (hdfs.exists(path)) hdfs.delete(path, true)
  }
}
