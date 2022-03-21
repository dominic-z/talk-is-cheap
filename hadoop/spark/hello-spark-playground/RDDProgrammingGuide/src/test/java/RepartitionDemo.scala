import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/5 7:19 下午
 */
class RepartitionDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
  @Test
  def repartitionTest():Unit={
    /*
    * repartition到底有什么用
    * spark的运行机制是，每次最多会运行最大并行度的计算，假如说最大并行度是20，那在map的时候，每次需要进行20个partition的运算
    * 假如说现在数据有20个partition，对其进行flatmap操作会导致每个partition的数据量扩大，有可能导致内存崩掉
    * 所以在flatmap之前，将数据repartition成40个，这样做的话，每次运行都只运行20个partition，每个partition的大小是原来的二分之一了，即使数据大小扩张，更有可能被机器hold住
    * */
    val rdd=sc.parallelize(Seq("a","b","c","d"))

    val repartitionRdd=rdd.repartition(10)

  }
}
