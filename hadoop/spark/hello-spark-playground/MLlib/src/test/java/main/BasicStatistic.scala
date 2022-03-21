package main

import org.apache.spark.ml.stat.{ChiSquareTest, Correlation}
import org.apache.spark.ml.linalg.{Matrix, Vectors,Vector}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions._
import org.junit.Test
import org.apache.spark.ml.stat.Summarizer

/**
 * @author dominiczhu
 * @date 2020/9/28 1:51 下午
 */
class BasicStatistic {

  val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  val sc = new SparkContext(sparkConf)
  val spark = SparkSession.builder().config(sparkConf).getOrCreate()
  import spark.implicits._

  @Test
  def correlationDemo(): Unit ={
    val data = Seq(
      Vectors.sparse(4, Seq((0, 1.0), (3, -2.0))),
      Vectors.dense(4.0, 5.0, 0.0, 3.0),
      Vectors.dense(6.0, 7.0, 0.0, 8.0),
      Vectors.sparse(4, Seq((0, 9.0), (3, 1.0)))
    )

    val df = data.map(Tuple1.apply).toDF("features")
    val Row(coeff1: Matrix) = Correlation.corr(df, "features").head
    println(s"Pearson correlation matrix:\n $coeff1")

    val Row(coeff2: Matrix) = Correlation.corr(df, "features", "spearman").head
    println(s"Spearman correlation matrix:\n $coeff2")
  }

  @Test
  def hypothesisDemo():Unit={
    val data = Seq(
      (0.0, Vectors.dense(0.5, 10.0)),
      (0.0, Vectors.dense(1.5, 20.0)),
      (1.0, Vectors.dense(1.5, 30.0)),
      (0.0, Vectors.dense(3.5, 30.0)),
      (0.0, Vectors.dense(3.5, 40.0)),
      (1.0, Vectors.dense(3.5, 40.0))
    )

    val df = data.toDF("label", "features")
    val chi = ChiSquareTest.test(df, "features", "label").head
    println(s"pValues = ${chi.getAs[Vector](0)}")
    println(s"degreesOfFreedom ${chi.getSeq[Int](1).mkString("[", ",", "]")}")
    println(s"statistics ${chi.getAs[Vector](2)}")
  }

  @Test
  def testSummarizer():Unit={
    val data = Seq(
      (Vectors.dense(2.0, 3.0, 5.0), 1.0),
      (Vectors.dense(4.0, 6.0, 7.0), 2.0)
    )

    val df = data.toDF("features", "weight")
    df.show()

    val (meanVal, varianceVal) = df.select(Summarizer.metrics("mean", "variance")
      .summary($"features", $"weight").as("summary"))
      .select("summary.mean", "summary.variance")
      .as[(Vector, Vector)].first()

    println(s"with weight: mean = ${meanVal}, variance = ${varianceVal}")

//    val (meanVal2, varianceVal2) = df.select(mean($"features"), variance($"features"))
//      .as[(Vector, Vector)].first()
//
//    println(s"without weight: mean = ${meanVal2}, sum = ${varianceVal2µ}")
  }
}
