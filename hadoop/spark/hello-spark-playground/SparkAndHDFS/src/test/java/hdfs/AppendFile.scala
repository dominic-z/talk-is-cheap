package hdfs

import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.Date

import org.apache.{commons, hadoop}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/21 上午10:18
 */
class AppendFile {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def appendStr(): Unit = {
    val logFile = new Path("fake_hdfs/append_file")
    val fs = FileSystem.get(logFile.toUri, sc.hadoopConfiguration)
    if (!fs.exists(logFile))
      fs.create(logFile).close()

    // 将字符串变成流，然后新增到目标hdfs
    val inputStream = commons.io.IOUtils.toInputStream(s"${new Date()}, aaaaa\n", StandardCharsets.UTF_8)
    val outputStream = fs.append(logFile)
//    val outputStream = fs.create(logFile) // 用create的也可
    hadoop.io.IOUtils.copyBytes(inputStream, outputStream, 4096, true)
  }

  @Test
  def appendFileContent(): Unit = {
    val logFile = new Path("hdfs://xxx")
    val fs = FileSystem.get(logFile.toUri, sc.hadoopConfiguration)
    if (!fs.exists(logFile))
      fs.create(logFile).close()

    // 将某个文件变为Input流，新增到目标hdfs

    val inputStream = new FileInputStream("localFile.txt")
    val outputStream = fs.append(logFile)
    hadoop.io.IOUtils.copyBytes(inputStream, outputStream, 4096, true)
  }
}
