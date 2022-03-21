package exceptions

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

import scala.util.Random

/**
 * @author dominiczhu
 * @date 2021/7/14 上午11:26
 */
/**
 *
 * @title HandleExceptions
 * @author dominiczhu
 * @date 2021/7/14 上午11:26
 * @version 1.0
 */
class HandleExceptions {

  @Test
  def handleExceptions():Unit={
    implicit val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
    implicit val sc: SparkContext = new SparkContext(sparkConf)
    implicit val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

    try{
      sc.parallelize(getSeq(1000000)).foreach(throw new RuntimeException())
    }catch {
      case exception: Exception=>
        println("Exp")
    }

  }


  def getSeq(num: Int) = {
    Seq.fill(num)(Random.nextInt)
  }

}
