package hdfs

import org.apache.hadoop.io.{IntWritable, LongWritable, NullWritable, Text}
import org.apache.hadoop.mapred
import org.apache.hadoop.mapreduce.lib.input
import org.apache.hadoop.mapreduce.lib.output
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/21 下午3:48
 */
class RDDSaveDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def saveAsHadoopFile(): Unit = {

    // 新api write
    val pairRDD = sc.parallelize(Seq((1, "嘿,1"), (2, "你好,2")), 1)

    val hadoopRDD = pairRDD.map(t => {
      (new IntWritable(t._1), new Text(t._2.getBytes("GBK")))
    })
    hadoopRDD.saveAsNewAPIHadoopFile("fake_hdfs/saveAsTextHadoopFile", classOf[IntWritable], classOf[Text],
      classOf[output.TextOutputFormat[IntWritable, Text]])


    pairRDD.map(s => (NullWritable.get(), new Text(s._2)))
      .saveAsNewAPIHadoopFile("fake_hdfs/saveAsTextHadoopFile_null", classOf[NullWritable], classOf[Text],
        classOf[output.TextOutputFormat[NullWritable, Text]])

    // read
    sc.hadoopFile("fake_hdfs/saveAsTextHadoopFile", classOf[mapred.TextInputFormat], classOf[LongWritable], classOf[Text])
      .map(t => {
        (t._1.get(), new String(t._2.getBytes, 0, t._2.getLength, "GBK"))
      })
      .collect()
      .foreach(println(_))

    // 新api
    sc.newAPIHadoopFile("fake_hdfs/saveAsTextHadoopFile", classOf[input.TextInputFormat], classOf[LongWritable], classOf[Text])
      .map(t => {
        (t._1.get(), new String(t._2.getBytes, 0, t._2.getLength, "GBK"))
      })
      .collect()
      .foreach(println(_))
  }


  @Test
  def textFileEmptyDir(): Unit = {
    //    如果目标文件夹已经存在，那么就会报错
    val dir = "fake_hdfs/saveAsText"
    val rdd = sc.parallelize(Array.range(0, 10))
    rdd.saveAsTextFile(dir)
  }

  @Test
  def saveEmptyRDD(): Unit = {
    //    如果目标文件夹已经存在，那么就会报错
    val dir = "fake_hdfs/save_empty_rdd_as_text"
    val rdd = sc.parallelize(Array.range(0, 10)).filter(_ < 0)
    rdd.saveAsTextFile(dir)
  }


  @Test
  def saveEmptyRddAsTextFile(): Unit = {
    //    如果目标文件夹已经存在，那么就会报错
    val dir = "fake_hdfs/saveEmptyRddAsText"
    val rdd = sc.textFile("fake_hdfs/empty.txt")
    rdd.saveAsTextFile(dir)
  }
}
