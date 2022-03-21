import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}


/*包括QuickStart部分代码*/
object Basics {
  def main(args: Array[String]): Unit = {
    val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
    val sc=new SparkContext(sparkConf)
    val spark=SparkSession.builder().config(sparkConf).getOrCreate()
    import spark.implicits._

    val textFile=spark.read.textFile("demo.txt")
    println("1"+textFile.count())
    println("2:g"+textFile.first())

    val linesWithSpark=textFile.filter(line=>line.contains("spark"))
    println("3:"+linesWithSpark.count())
    println("4:"+textFile.map(line=>line.split(",").length).reduce((a, b)=>a+b))

    val wordCounts=textFile.flatMap(line=>line.split(",")).groupByKey(identity).count()
    println("5:"+wordCounts.collect().toList)

  }

}
