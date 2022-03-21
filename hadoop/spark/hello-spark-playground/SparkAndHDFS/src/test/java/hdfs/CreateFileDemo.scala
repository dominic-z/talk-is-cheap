package hdfs

import java.io.IOException

import org.apache.hadoop.fs.{FSDataOutputStream, FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/17 下午7:47
 */
class CreateFileDemo {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def createEmptyFile(): Unit = {
    val hdfs = FileSystem.get(sc.hadoopConfiguration)
    hdfs.create(new Path("fake_hdfs/_SUCCESS"))
  }

  @Test
  def writeToFile(): Unit = {
    val hdfs = FileSystem.get(sc.hadoopConfiguration)
    val outputStream = hdfs.create(new Path("fake_hdfs/write_file"))
    outputStream.writeBytes("write")
    outputStream.close() // 要先close这个outputStream，如果先close fs，写入不会成功
    hdfs.close()

    val hdfs2 = FileSystem.get(sc.hadoopConfiguration)
    val outputStream2 = hdfs2.create(new Path("fake_hdfs/write_file"))
    outputStream2.writeBytes("write2")
    outputStream2.close() // 要先close这个outputStream，如果先close fs，写入不会成功
    hdfs2.close()
  }

  @Test
  def createFileInDosentExistDir(): Unit = {
    //    集群测试，会自动创建父目录，如果父目录不存在的话
    val path = new Path("fake_hdfs/doesnt_exist_dir/create_empty_file")
    val hdfs = FileSystem.get(path.toUri, sc.hadoopConfiguration)
    hdfs.create(path)
  }

  def createHdfsFile(): Unit = {
    val path = new Path("hdfs://xxx/xxx")
    val hdfs = FileSystem.get(path.toUri, sc.hadoopConfiguration)
    var outputStream: FSDataOutputStream = null
    try {
      outputStream = hdfs.create(path, false)
      outputStream.writeUTF("Hei there")
    } catch {
      case _: IOException =>
        println("file already exists")
    } finally {
      outputStream.close()
    }
  }
}
