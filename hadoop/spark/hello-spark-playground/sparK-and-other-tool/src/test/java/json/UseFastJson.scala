package json

import com.alibaba.fastjson.JSON
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test

import scala.collection.JavaConverters._
import scala.collection.mutable
/**
 * @author dominiczhu
 * @date 2021/8/21 下午9:22
 */
/**
 *
 * @title UseJackson
 * @author dominiczhu
 * @date 2021/8/21 下午9:22
 * @version 1.0
 */
class UseFastJson {


  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  @Test
  def useFastJson():Unit={

//    val list:java.util.List[String] = mutable.ArrayBuffer("123", "abc").asJava
//    val listJson = JSON.toJSONString(list)
//    println(listJson)
//
//    sc.parallelize(Array(Array("123","234"),Array("bcs","abc")))
//      .map(arr=>JSON.toJSONString(arr))
//      .collect()
//      .foreach(println)



  }

}
