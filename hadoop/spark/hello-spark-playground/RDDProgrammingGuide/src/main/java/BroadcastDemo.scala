import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

object BroadcastDemo {
  def main(args: Array[String]): Unit = {
    val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
    val sc=new SparkContext(sparkConf)
    val spark=SparkSession.builder().config(sparkConf).getOrCreate()
    val broadcastVar = sc.broadcast(Array(1, 2, 3))

    println(broadcastVar.value.mkString(","))

  }
}
