package hdfs

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/23 下午4:35
 *//**
 *
 * @title: ListDemo
 * @Author Tan
 * @Date: 2020/11/23 下午4:35
 * @Version 1.0
 */
class ListDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def listStatus():Unit={
    //    遍历当前目录下的文件和文件夹
    val hdfs = FileSystem.get(sc.hadoopConfiguration)
    val statusList=hdfs.listStatus(new Path("fake_hdfs"))
    statusList.foreach(status=>println(status.toString))
  }

  @Test
  def listFiles():Unit={
//    遍历文件
    val hdfs = FileSystem.get(sc.hadoopConfiguration)
    val remoteIterator=hdfs.listFiles(new Path("fake_hdfs"),false)
    while(remoteIterator.hasNext){
      println(remoteIterator.next().getPath)
    }
    println("=============================")
    val allRemoteIterator=hdfs.listFiles(new Path("fake_hdfs"),true)
    while(allRemoteIterator.hasNext){
      println(allRemoteIterator.next().getPath)
    }
  }
}
