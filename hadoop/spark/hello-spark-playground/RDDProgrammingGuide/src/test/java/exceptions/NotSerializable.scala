package exceptions

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.junit.Test

import scala.util.Random

/**
 * @author dominiczhu
 * @date 2021/7/14 上午11:08
 */
/**
 *
 * @title NotSerializable
 * @author dominiczhu
 * @date 2021/7/14 上午11:08
 * @version 1.0
 */
class NotSerializable {

  @Test
  def notSerializableException(): Unit = {


    implicit val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
    implicit val sc: SparkContext = new SparkContext(sparkConf)
    implicit val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    val holder = new ContextHolder(sc, 10)

    val rdd = sc.parallelize(Seq.range(1L, 10L))

    holder.doMap(rdd)
  }


  def getSeq(num: Int): Seq[Int] = {
    Seq.fill(num)(Random.nextInt)
  }

}

class ContextHolder(val sc: SparkContext, val i: Long) extends Serializable {


  def doMap(rdd: RDD[Long]): RDD[Long] = {
    // 如下map需要使用i，但是各个executor唯一获取i的方式就是将this对象序列化分发出去，但是this对象的sc是一个不可序列化的对象，因此报错
//    rdd.map(l => l + i) // 等价于rdd.map(l => l + this.i)

    // 如下同理
//    rdd.map(aMethod) // 等价于rdd.map(this.aMethod(_))

    // 下面方法不会报错，因为this.aFunc用到了i
    rdd.map(aFunc)
    // TMD下面的方法又不会报错，只能理解为spark针对function做了特殊处理了
    rdd.map(bFunc)
  }

  def aMethod(l: Long): Long = {
    l + 1
  }

  val aFunc = (l: Long) => l + i
  val bFunc = (l: Long) => l + 1

}
