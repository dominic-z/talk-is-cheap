import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/20 下午8:54
 */
class DealWithError {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def dealWithError(): Unit = {
    val rdd = sc.parallelize(Array.range(0, 10).toSeq)

//    在真实的spark环境里，错误会被回传到driver里，如果driver和worker不在同一台机器上，那么这个任务是会成功的，因为回传的错误被driver catch了
//    但如果driver和worker位于同一台机器上，那实际上就是会失败的。
    try {
      val res = rdd.map(i => {
        i / 0
      })
      res.collect()
    } catch {
      case e: Exception => {
        println("catch error")
      }
    }
  }
}
