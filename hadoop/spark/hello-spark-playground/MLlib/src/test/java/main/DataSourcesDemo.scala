package main

import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/9/28 2:21 下午
 */
class DataSourcesDemo {
  val sparkConf = new SparkConf().setAppName("local").setMaster("local[5]")
  val sc = new SparkContext(sparkConf)
  val spark = SparkSession.builder().config(sparkConf).getOrCreate()
  import spark.implicits._

  @Test
  def testImageDataSource():Unit={
    val df = spark.read.format("image").option("dropInvalid", true).load("data/mllib/images/origin/kittens")
    println(df)
    df.show(1)
    df.select("image.origin", "image.width", "image.height").show(truncate=false)
  }

  @Test
  def testLibSvm():Unit={
    val df = spark.read.format("libsvm").option("numFeatures", "780").load("data/mllib/sample_libsvm_data.txt")
    println(df)
    df.show(10)
  }

  @Test
  def testVector():Unit={
    val sparseVectorDf = spark.read.format("libsvm").option("numFeatures", "780").load("data/mllib/sample_libsvm_data.txt")
    val arrDs=sparseVectorDf.map(row=>{
      val label=row.getDouble(0)
      val featureArr=row.getAs[SparseVector](1) // 这个类型是ml这个包里的SparseVector，不是mllib里的
      (label,featureArr.toArray)
    })

    arrDs.show(10)
  }

  @Test
  def testCsv():Unit={
//    默认情况下，spark读csv是认为没有head的，并且读出来的结果为string类型
    val csvDf=spark.read.format("csv").load("data/iris.csv")
    csvDf.show(10)
    print(csvDf.schema)
  }

  @Test
  def testLibSvmOptions():Unit={
    /*
    * options参数在哪看，官方文档对options的描述位置比较分散，我的方法是去api文档之中搜索libsvm，搜索结果中偶尔会找到一些信息
    * 例如http://spark.apache.org/docs/latest/api/scala/org/apache/spark/ml/source/libsvm/LibSVMDataSource.html
    * 并且spark的libsvm必须是zero-based的，文件中如果出现0:1会报错
    * */

    val libsvmDf=spark.read.format("libsvm").option("numFeatures","4").option("vectorType","dense").load("data/simple.libsvm")
    libsvmDf.show(10)
  }

  @Test
  def testCsvOptions():Unit={
    /*
    * http://spark.apache.org/docs/latest/api/scala/org/apache/spark/sql/DataFrameReader.html csv()中记录了csv的option
    * */
    val csvDf=spark.read.format("csv").option("header",value = true).load("data/iris.csv")
    csvDf.show(10)
  }
}
