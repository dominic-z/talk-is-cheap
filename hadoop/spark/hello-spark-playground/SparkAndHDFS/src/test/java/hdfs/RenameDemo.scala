package hdfs

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/21 下午4:40
 */
class RenameDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  //  本机之中，将文件挪到一个存在的文件夹下面会失败
  //  但是在我公司的集群上，这么做会失败

  @Test
  def renameDir(): Unit = {
    val dirPath = "fake_hdfs/dir_for_rename_src"
    val srcDir = new Path(dirPath)
    val hdfs = org.apache.hadoop.fs.FileSystem.get(srcDir.toUri, sc.hadoopConfiguration)
    println(s"success ${hdfs.rename(srcDir, new Path("fake_hdfs/dir_for_rename_dst"))}")
  }

  @Test
  def renameFile(): Unit = {
    val dirPath = "fake_hdfs/file_for_rename_src"
    val srcDir = new Path(dirPath)
    val hdfs = org.apache.hadoop.fs.FileSystem.get(srcDir.toUri, sc.hadoopConfiguration)
    println(s"success ${hdfs.rename(srcDir, new Path("fake_hdfs/dontexists/file_for_rename_dst"))}")
  }


  //  以下行为是在公司内集群测得的 其实rename就是mv，操作结果和mv一模一样 规则也一样
  val dirForRenamePath = "hdfs://xxx"
  val fileForRenamePath = "hdfs://xxx"

  //  将文件夹挪到一个不存在的文件夹里面会失败
  def renameDirIntoDoesntExistDir()(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val srcDir = new Path(dirForRenamePath)
    val dstDirPath = "hdfs://xxx"
    val dstDir = new Path(dstDirPath)
    val fs = FileSystem.get(srcDir.toUri, sc.hadoopConfiguration)

    println(s"success: ${fs.rename(srcDir, dstDir)}")
  }

  //  将文件挪到一个不存在的文件夹里面会失败
  def renameFileIntoDoesntExistDir()(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val srcFile = new Path(fileForRenamePath)
    val dstFilePath = "hdfs://xxx"
    val dstDir = new Path(dstFilePath)
    val fs = FileSystem.get(srcFile.toUri, sc.hadoopConfiguration)
    println(s"success: ${fs.rename(srcFile, dstDir)}")
  }


  //  将文件夹挪到一个已经存在的文件夹，会导致源文件夹被挪动到目标文件夹里面
  def renameDirToExistDir()(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val srcDir = new Path(dirForRenamePath)
    val dstDirPath = "hdfs://xxx"
    val dstDir = new Path(dstDirPath)
    val fs = FileSystem.get(srcDir.toUri, sc.hadoopConfiguration)
    println(s"success: ${fs.rename(srcDir, dstDir)}")
  }

  //  将文件夹挪到一个不存在的文件夹(父目录存在)，会成功
  def renameDirToDosentExistDir()(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val srcDir = new Path(dirForRenamePath)
    val dstDirPath = "hdfs://xxx"
    val dstDir = new Path(dstDirPath)
    val fs = FileSystem.get(srcDir.toUri, sc.hadoopConfiguration)
    println(s"success: ${fs.rename(srcDir, dstDir)}")
  }

  //  将文件挪到一个已经存在的文件，会失败
  def renameFileToExistFile()(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val srcFile = new Path(fileForRenamePath)
    val dstFilePath = "hdfs://xxx"
    val dstDir = new Path(dstFilePath)
    val fs = FileSystem.get(srcFile.toUri, sc.hadoopConfiguration)
    println(s"success: ${fs.rename(srcFile, dstDir)}")
  }

  //  将文件挪到一个已经存在的文件 会成功
  def renameFileIntoExistDir()(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val srcFile = new Path(fileForRenamePath)
    val dstFilePath = "hdfs://xxx"
    val dstDir = new Path(dstFilePath)
    val fs = FileSystem.get(srcFile.toUri, sc.hadoopConfiguration)
    println(s"success: ${fs.rename(srcFile, dstDir)}")
  }
}
