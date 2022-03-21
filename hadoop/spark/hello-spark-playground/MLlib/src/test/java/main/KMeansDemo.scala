package main

import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.evaluation.ClusteringEvaluator
import org.apache.spark.ml.linalg.DenseVector
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2021/5/20 下午3:24
 */
/**
 *
 * @title KMeansDemo
 * @author dominiczhu
 * @date 2021/5/20 下午3:24
 * @version 1.0
 */
class KMeansDemo {

  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  val sc = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._

  @Test
  def fitDemo(): Unit = {


    // Loads data.
    //    val dataset = spark.read.format("libsvm").load("data/mllib/sample_kmeans_data.txt")
    val dataset = sc.textFile("data/mllib/kmeans_data.txt")
      .map(line => {
        val row = line.split(" ")
        // tmd一定记住，由于下面的KMeans是org.apache.spark.ml里的，因此这个Vector也一定要是org.apache.spark.ml.linalg里的
        val featureVector = new DenseVector(row.map(_.toDouble))
        (featureVector, 1d)
      }).toDF("features1", "other")
    dataset.show()

    // Trains a k-means model.
    val kmeans = new KMeans().setK(2).setSeed(1L).setFeaturesCol("features1")
    val model = kmeans.fit(dataset)

    // Make predictions
    val predictions = model.transform(dataset)
    println(s"show predictions")
    predictions.show()

    // Evaluate clustering by computing Silhouette score
    val evaluator = new ClusteringEvaluator().setFeaturesCol("features1")

    val silhouette = evaluator.evaluate(predictions)
    println(s"Silhouette with squared euclidean distance = $silhouette")

    // Shows the result.
    println("Cluster Centers: ")
    model.clusterCenters.foreach(println)
  }

}
