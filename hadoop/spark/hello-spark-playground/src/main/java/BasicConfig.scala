import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

 object BasicConfig {
  def main(args: Array[String]): Unit = {
    val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
    val sc=new SparkContext(sparkConf)
    val spark=SparkSession.builder().config(sparkConf).getOrCreate()
    import spark.implicits._

  }

  def getSparkAndSc:(SparkContext,SparkSession)={
    val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
    val sc=new SparkContext(sparkConf)
    val spark=SparkSession.builder().config(sparkConf).getOrCreate()
    (sc,spark)
  }
}
