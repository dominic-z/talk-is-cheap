package gettingStarted

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

import scala.io.Source

class CreatingDataFrames {
  val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
  val sc=new SparkContext(sparkConf)
  val spark=SparkSession.builder().config(sparkConf).getOrCreate()
  import spark.implicits._
  @Test
  def createDf(): Unit = {

    val df = spark.read.json("people.json")

    // Displays the content of the DataFrame to stdout
    df.show()

    df.printSchema()
    df.select("name").show()
    df.select($"name",$"age"+1).show()

    df.filter($"age">21).show()


    df.createOrReplaceTempView("people")

    val sqlDF = spark.sql("SELECT * FROM people")
    sqlDF.show()

    df.createGlobalTempView("people")
    spark.sql("SELECT * FROM global_view.people").show()


  }


}
