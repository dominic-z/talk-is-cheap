import ml.dmlc.xgboost4j.scala.spark.{XGBoostClassificationModel, XGBoostClassifier}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/10/16 5:29 下午
 */
class ClassificationDemo {
  val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  val sc = new SparkContext(sparkConf)
  val spark = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._

  @Test
  def testMultiClassificationSimpleTrain(): Unit = {
    val schema = new StructType(Array(
      StructField("sepal length", DoubleType, true),
      StructField("sepal width", DoubleType, true),
      StructField("petal length", DoubleType, true),
      StructField("petal width", DoubleType, true),
      StructField("class", StringType, true)))
    val rawInput = spark.read.schema(schema).csv("data/iris.csv")
    rawInput.repartition(3)

    val stringIndexer = new StringIndexer().
      setInputCol("class").
      setOutputCol("classIndex").
      fit(rawInput)
    val labelTransformed = stringIndexer.transform(rawInput).drop("class")

    labelTransformed.show(10)

    val vectorAssembler = new VectorAssembler().
      setInputCols(Array("sepal length", "sepal width", "petal length", "petal width")).
      setOutputCol("features")
    val xgbInput = vectorAssembler.transform(labelTransformed).select("features", "classIndex")
    //    说明xgboost的输入feature也得是org.apache.spark.ml.linalg.Vector
    print(xgbInput.schema)

    xgbInput.show(10)

    val xgbParam = Map("eta" -> 0.1f,
      "max_depth" -> 2,
      "objective" -> "multi:softprob",
      "num_class" -> 3,
      "num_round" -> 100,
      "num_workers" -> 2)
    val xgbClassifier = new XGBoostClassifier(xgbParam).
      setFeaturesCol("features").
      setLabelCol("classIndex")

    val xgbClassificationModel = xgbClassifier.fit(xgbInput)
    val rootDir = "rootDir"
    xgbClassificationModel.write.overwrite().save(s"${rootDir}/xgb_model.model")

    val xgbClassificationModel2 = XGBoostClassificationModel.load(s"${rootDir}/xgb_model.model")
    val preResult = xgbClassificationModel2.transform(xgbInput)
    println(preResult.schema)
    /*
    * StructType(StructField(features,org.apache.spark.ml.linalg.VectorUDT@3bfc3ba7,true), StructField(classIndex,DoubleType,false), StructField(rawPrediction,org.apache.spark.ml.linalg.VectorUDT@3bfc3ba7,true), StructField(probability,org.apache.spark.ml.linalg.VectorUDT@3bfc3ba7,true), StructField(prediction,DoubleType,false)
    * */
    preResult.show(20)

  }

  //    注意，如果objective是二分类，binary:logistic的话，不应该指定num_class 否则报错
  @Test
  def testBinaryClassificationSimpleTrain(): Unit = {

    val rawInput = spark.read.format("libsvm").option("vectorType", "sparse").csv("data/sample_binary_classification_data.txt")

    /*
    * Note

Missing values with Spark’s VectorAssembler

If given a Dataset with enough features having a value of 0 Spark’s VectorAssembler transformer class will return a SparseVector where the absent values are meant to indicate a value of 0. This conflicts with XGBoost’s default to treat values absent from the SparseVector as missing. The model would effectively be treating 0 as missing but not declaring that to be so which can lead to confusion when using the trained model on other platforms. To avoid this, XGBoost will raise an exception if it receives a SparseVector and the “missing” parameter has not been explicitly set to 0. To workaround this issue the user has three options:

1. Explicitly convert the Vector returned from VectorAssembler to a DenseVector to return the zeros to the dataset. If doing this with missing values encoded as NaN, you will want to set setHandleInvalid = "keep" on VectorAssembler in order to keep the NaN values in the dataset. You would then set the “missing” parameter to whatever you want to be treated as missing. However this may cause a large amount of memory use if your dataset is very sparse.

2. Before calling VectorAssembler you can transform the values you want to represent missing into an irregular value that is not 0, NaN, or Null and set the “missing” parameter to 0. The irregular value should ideally be chosen to be outside the range of values that your features have.

3. Do not use the VectorAssembler class and instead use a custom way of constructing a SparseVector that allows for specifying sparsity to indicate a non-zero value. You can then set the “missing” parameter to whatever sparsity indicates in your Dataset. If this approach is taken you can pass the parameter "allow_non_zero_for_missing_value" -> true to bypass XGBoost’s assertion that “missing” must be zero when given a SparseVector.

Option 1 is recommended if memory constraints are not an issue. Option 3 requires more work to get set up but is guaranteed to give you correct results while option 2 will be quicker to set up but may be difficult to find a good irregular value that does not conflict with your feature values.
    * */

//    当是二分类的时候，不可以添加num_class参数，否则报错
//    上面那段话是官方文档截下来的，意思是如果是自己设计的输入输出，没有用vectorAssemble，并且特征向量是SparseVector，那就要手动指定缺失值，例如missing=0
    val xgbParam = Map("eta" -> 0.1f,
      "max_depth" -> 2,
      "objective" -> "binary:logistic",
      "missing" -> 0,
      "num_round" -> 100,
      "num_workers" -> 2)
    val xgbClassifier = new XGBoostClassifier(xgbParam).
      setFeaturesCol("features").
      setLabelCol("label")
    val xgbClassificationModel = xgbClassifier.fit(rawInput)
  }
}
