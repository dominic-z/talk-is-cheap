package json

import com.google.gson.Gson
import domain.Animal
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
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
class UseJackson {


  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  // jackson和gson的对象都无法序列化
  @Test
  def useObjectMapper(): Unit = {

    val objectMapper = new ObjectMapper()
    val listJson = objectMapper.writeValueAsString(mutable.ArrayBuffer("123", "abc").asJava)
    println(listJson)

    //    sc.parallelize(Array(Array("123","234"),Array("bcs","abc")))
    //      .map(arr=>objectMapper.writeValueAsString(arr))
    //      .collect()
    //      .foreach(println)

    sc.parallelize(Array(Array("123", "234"), Array("bcs", "abc")))
      .mapPartitions(iterator => {
        val objectMapper = new ObjectMapper()
        val jsons = mutable.ArrayBuffer[String]()
        while (iterator.hasNext) {
          val arr = iterator.next()
          val json = objectMapper.writeValueAsString(mutable.ArrayBuffer(arr: _*).asJava)
          jsons.append(json)
        }
        jsons.iterator
      })
      .collect()
      .foreach(println)


  }

}
