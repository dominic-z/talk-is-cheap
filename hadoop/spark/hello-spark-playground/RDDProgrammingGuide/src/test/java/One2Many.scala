import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/11/10 5:36 下午
 */
class One2Many {
  implicit val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  implicit val sc = new SparkContext(sparkConf)
  implicit val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._
  @Test
  def one2many():Unit={
    val rdd=sc.parallelize(Array.range(10,15).map(i=>i.toString).toSeq)
    val res=rdd.flatMap(s=>s.toCharArray).collect()
    println(res.toSeq)
  }

  @Test
  def many2one():Unit={
//    dataframe操作
  }
}
