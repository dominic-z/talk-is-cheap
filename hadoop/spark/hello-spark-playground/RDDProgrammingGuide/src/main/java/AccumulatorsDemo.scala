import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

object AccumulatorsDemo {
  def main(args: Array[String]): Unit = {
    val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
    val sc=new SparkContext(sparkConf)
    val spark=SparkSession.builder().config(sparkConf).getOrCreate()
    val accum = sc.longAccumulator("My Accumulator")

    sc.parallelize(Array(1, 2, 3, 4)).foreach(x => accum.add(x))
    println(accum.value)
  }

}
