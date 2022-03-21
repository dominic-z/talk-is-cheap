import cases.SumCase
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

object RDDDemo {
  val sparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._

  def main(args: Array[String]): Unit = {
    simpleDemo
  }

  def simpleDemo(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val data = Array(1, 2, 3, 4, 5)
    val distData = sc.parallelize(data)

    val distFile = sc.textFile("demo.txt")
    val lineLengths = distFile.map(line => line.split(",").length)
    print(lineLengths)
    val totalLines = lineLengths.reduce((a, b) => a + b)
    print(totalLines)

    val pairs = distFile.flatMap(line => line.split(",")).map(s => (s, 1))
    val counts = pairs.reduceByKey((a, b) => a + b)
    println(counts.collect().mkString(","))

    val distPartitions = distData.mapPartitions[Int](_ => {
      Iterator(1, 2, 3)
    })
    println(distPartitions.collect().mkString(","))
  }

  def reduceByKeyDemo(implicit sc: SparkContext, spark: SparkSession): Unit = {
    val data = Array((1,1), (2,2), (1,3))
    val rdd = sc.parallelize(data)

  }
}
