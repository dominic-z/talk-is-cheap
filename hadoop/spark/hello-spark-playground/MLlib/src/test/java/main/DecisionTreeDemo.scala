package main

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassificationModel
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorIndexer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.functions._

/**
 * @author dominiczhu
 * @date 2020/9/28 4:25 下午
 */
class DecisionTreeDemo {
  val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  val sc = new SparkContext(sparkConf)
  val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._

  @Test
  def testDecisionTreeDemo(): Unit = {
    // Load the data stored in LIBSVM format as a DataFrame. 文件之中的1:10，第0个特征取值为10
    val data = spark.read.format("libsvm").load("data/mllib/sample_libsvm_data.txt")
    data.show()
    // Index labels, adding metadata to the label column.
    // Fit on whole dataset to include all labels in index.
    // 然后将某个string转化成一个double类型的index，然后在原有基础上添加一列
    val labelIndexer = new StringIndexer()
      .setInputCol("label")
      .setOutputCol("indexedLabel")
      .fit(data)
    val example1 = labelIndexer.transform(data).collect()
    println(example1.take(1).toSeq)

    // Automatically identify categorical features, and index them.
    // 这个玩意的功能就是从数据中抽取类别特征与连续特征，如果某一列取值数目大于4的会被当做连续值，不做映射
    // 而如果小于4那么就会进行映射，映射规则存储于这个featureIndexer的categoryMap
    // 但实际上后面的算法模型是不知道谁是连续谁是不连续的，这个玩意的功能就是一个将{133,144}变成{0,1}的东西
    val featureIndexer = new VectorIndexer()
      .setInputCol("features")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4) // features with > 4 distinct values are treated as continuous.
      .fit(data)
    val example2 = featureIndexer.transform(data).collect()
    println(example2.take(1).toSeq)
    // Split the data into training and test sets (30% held out for testing).
    val Array(trainingData, testData) = data.randomSplit(Array(0.7, 0.3))

    // Train a DecisionTree model.
    val dt = new DecisionTreeClassifier()
      .setLabelCol("indexedLabel")
      .setFeaturesCol("indexedFeatures")

    // Convert indexed labels back to original labels.
    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(labelIndexer.labels)

    // Chain indexers and tree in a Pipeline.
    val pipeline = new Pipeline()
      .setStages(Array(labelIndexer, featureIndexer, dt, labelConverter))

    // Train model. This also runs the indexers.
    val model = pipeline.fit(trainingData)

    // Make predictions.
    val predictions = model.transform(testData)

    // Select example rows to display.
    predictions.select("predictedLabel", "label", "features").show(5)

    // Select (prediction, true label) and compute test error.
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("indexedLabel")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)
    println(s"Test Error = ${(1.0 - accuracy)}")

    val treeModel = model.stages(2).asInstanceOf[DecisionTreeClassificationModel]
    println(s"Learned classification tree model:\n ${treeModel.toDebugString}")
  }

  @Test
  def justDamnFit(): Unit = {
    //    输入进各种模型里特征必须是Vector的，可以是DenseVector也可以是SparseVector，构造方法可以查看api
    val data = spark.read.format("libsvm").load("data/mllib/sample_libsvm_data.txt")
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label")
      .setFeaturesCol("features")
    val pipeline = new Pipeline()
      .setStages(Array(dt))
    val model = dt.fit(data) // 报错，不可以输入arr

//    val model = pipeline.fit(data)
    val predictions = model.transform(data)
    predictions.show(10)
  }

  @Test
  def modelInputWithArray(): Unit = {
    //    输入进各种模型里特征必须是Vector的，可以是DenseVector也可以是SparseVector，构造方法可以查看api
    val data = spark.read.format("libsvm").load("data/mllib/sample_libsvm_data.txt").map(row => {
      val label = row.getDouble(0)
      val featureArr = row.getAs[SparseVector](1).toArray
      (label,featureArr)
    })
    val dt = new DecisionTreeClassifier()
      .setLabelCol("_1")
      .setFeaturesCol("_2")
    val pipeline = new Pipeline()
      .setStages(Array(dt))// 报错，不可以输入arr

    val model = pipeline.fit(data)

    val predictions = model.transform(data)
    predictions.show(10)
  }
}
