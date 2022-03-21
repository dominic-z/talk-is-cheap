import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

import scala.collection.mutable
import scala.util.Random

/**
 * @author dominiczhu
 * @date 2020/11/19 下午2:29
 */
class OpRDD {

  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()


  @Test
  def parallelize():Unit={

    val rdds = mutable.ArrayBuffer[RDD[Long]]()

    val func = (s:Int,e:Int)=>{
      println("123")
      rdds.append(sc.parallelize(Seq.range(s,e)))

    }


    for(i<-Array.range(0,50,10)){
      func(i,i+10)
    }

    println(sc.union(rdds).collect())

  }

  @Test
  def emptyRddReduceByKey(): Unit = {
    val data = Array((1, 1), (2, 2), (1, 3))
    val rdd = sc.parallelize(data).filter(t => t._1 < 0)
    println(rdd.collect().toSeq)

    println(rdd.reduceByKey((a, b) => a + b).collect().toSeq)
  }

  @Test
  def unionEmptyRddArray(): Unit = {
    //    不会报错
    val arr = Array[RDD[String]]()
    println(sc.union(arr).collect().mkString(","))
  }

  @Test
  def mapDemo(): Unit = {
    val data = Array((1, 1), (2, 2), (1, 3))
    val rdd = sc.parallelize(data).map((_, 1))
    println(rdd.collect().toSeq)


    // map的返回值尽量不要使用类型不明确的值，下面的返回值在本机运行不会报错，但是曾经在线上使用时，类型推断错误导致报错了；
    val mapped = sc.parallelize(data).map(t=>{
      if(t._1%2==0){
        (1L,2)
      }else{
        (1,2)
      }
    })

    println(mapped.collect().toSeq)

  }

  @Test
  def mapReturnAnyDemo(): Unit = {
    // 会报错，
    val data = Array((1, 1), (2, 2), (1, 3))
    val rdd = sc.parallelize(data).map(t => {
      val v1 = t._1
      if (v1 == 1)
        "1"
      else
        v1
    })
    println(rdd.collect().toSeq)
  }


  @Test
  def sampleByKeyDemo(): Unit = {
    val data = Array((1, 1), (2, 2), (1, 3))
    val rdd = sc.parallelize(data)

    val fractionMap = Map((1, 0.3d), (2, 3d))

    // 大于1都会被视为1
    val res = rdd.sampleByKey(withReplacement = false, fractions = fractionMap)
    println(res.collect().toSeq)

  }

  @Test
  def sortByDemo(): Unit = {
    val data = Array.range(0, 10).map(i => (Random.nextFloat(), Random.nextInt(10)))
    println(data.toSeq)
    val rdd = sc.parallelize(data)

    // sortBy可以通过修改隐含参数中的ordering来修改对比大小的方式
    println(rdd.sortBy(t => (t._2,t._1)).zipWithIndex.collect().toSeq)
  }



  @Test
  def join():Unit={


    val left = (Array.range(0, 10)++Array.range(8, 10)).map(i => (i, Random.nextInt(10)))
    val right = Array.range(5, 15).map(i => (i, Random.nextInt(10)))

    val leftRDD = sc.parallelize(left)
    val rightRDD = sc.parallelize(right)

    leftRDD.leftOuterJoin(rightRDD).collect().foreach(println)


  }
}
