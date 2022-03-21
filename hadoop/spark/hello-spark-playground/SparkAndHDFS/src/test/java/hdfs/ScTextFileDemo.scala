package hdfs

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/23 下午5:03
 *//**
 *
 * @title: TextFileDemo
 * @Author Tan
 * @Date: 2020/11/23 下午5:03
 * @Version 1.0
 */
class ScTextFileDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def textFile1(): Unit = {
    //    如果下面的读取行为，没有文件匹配，就会报个错，textFile接受通配符，但是必须有文件匹配得上
    val rdd1 = sc.textFile("fake_hdfs/for_textfile/*")
    println(rdd1.collect().mkString(";"))
  }

  @Test
  def textFile2(): Unit = {
    //    如果下面的读取行为，没有文件匹配，就会报个错，textFile接受通配符，但是必须有文件匹配得上
    //    操蛋的是_success文件不会被读取，下划线开头的都不会被textFile识别
    val rdd2 = sc.textFile("fake_hdfs/only_success/*")
    println(rdd2.collect().mkString(";"))

  }


  @Test
  def textFileEmptyDir(): Unit = {
    //    如果sc来读取文件没有文件match得上，match上，会报错
    val emptyDirPath = "fake_hdfs/empty_dir"
    val rdd = sc.textFile(s"${emptyDirPath}/*")
    println("read empty dir")
    println(rdd.collect().mkString("\n"))
  }

  @Test
  def textFileEmptyDirButADir(): Unit = {
    //    只要没有text文件match，也会报错
    val emptyDirPath = "fake_hdfs/empty_dir_but_a_dir"
    val rdd = sc.textFile(s"${emptyDirPath}/*")
    println("read empty dir")
    println(rdd.collect().mkString("\n"))
  }


  @Test
  def textFileWithADir(): Unit = {
    //    即使这个文件夹下面有一个文件夹，那textfile也会读取这个文件夹下面的文件。
    val emptyDirPath = "fake_hdfs/text_file_and_dir"
    val rdd = sc.textFile(s"${emptyDirPath}/*")
    println("text_file_and_dir")
    println(rdd.collect().mkString("\n"))
  }


  @Test
  def textFileWithADirRecrusive(): Unit = {
    //    会报错，因为textFile只会搜索一层
    // 如果textFile的输入是path，那么会读取path下的文件、以及path下一级文件夹里的文件，如果里面再嵌套文件夹，就不行了
    val recDir = "fake_hdfs"
    val rdd = sc.textFile(s"${recDir}/*")
    println("textFileWithADirRecrusive")
    println(rdd.collect().mkString("\n"))
  }


  @Test
  def textFileSeveralFile(): Unit = {
    // 读取多个文件，逗号分隔符
    val recDir = "fake_hdfs"
    val rdd = sc.textFile(s"${recDir}/append_file,${recDir}/write_file")
    println("textFileSeveralFile")
    println(rdd.collect().mkString("\n"))
  }
}
