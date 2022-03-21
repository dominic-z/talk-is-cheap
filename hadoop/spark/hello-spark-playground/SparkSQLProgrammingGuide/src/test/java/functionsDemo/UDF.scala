package functionsDemo

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/9/19 4:22 下午
 *       Test文件读取的基本路径是本model，而不是外面的project
 */
class UDF {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._

  @Test
  def udfDocDemo(): Unit = {
    // Define and register a zero-argument non-deterministic UDF
    // UDF is deterministic by default, i.e. produces the same result for the same input.
    val random = udf(() => Math.random())
    spark.udf.register("random", random.asNondeterministic())
    spark.sql("SELECT random()").show()
    // +-------+
    // |UDF()  |
    // +-------+
    // |xxxxxxx|
    // +-------+

    // Define and register a one-argument UDF
    val plusOne = udf((x: Int) => x + 1)
    spark.udf.register("plusOne", plusOne)
    spark.sql("SELECT plusOne(5)").show()
    // +------+
    // |UDF(5)|
    // +------+
    // |     6|
    // +------+

    // Define a two-argument UDF and register it with Spark in one step
    spark.udf.register("strLenScala", (_: String).length + (_: Int))
    spark.sql("SELECT strLenScala('test', 1)").show()
    // +--------------------+
    // |strLenScala(test, 1)|
    // +--------------------+
    // |                   5|
    // +--------------------+

    // UDF in a WHERE clause
    spark.udf.register("oneArgFilter", (n: Int) => {
      n > 5
    })
    spark.range(1, 10).createOrReplaceTempView("test")
    spark.sql("SELECT * FROM test WHERE oneArgFilter(id)").show()
    // +---+
    // | id|
    // +---+
    // |  6|
    // |  7|
    // |  8|
    // |  9|
    // +---+

  }

  @Test
  def useUdf(): Unit = {
    val df = spark.read.json("employeesForUdf.json")
    val addOneColumn = udf((x: Int) => x + 1)
    val addConst = udf(() => 1)
    df.select($"name", $"salary", addOneColumn($"salary").as("salaryPlus1")).show()
    df.withColumn("salaryPlus1", addOneColumn($"salary")).show()
    df.withColumn("const", addConst()).show()
  }

  @Test
  def litDemo(): Unit = {
    val df = spark.read.json("employeesForUdf.json")
    val withNull = df.withColumn("null_col", lit(null))
    withNull.collect().foreach(row => {
      val str = row.getAs[String]("null_col")
      println(str == null)
      println(str)
    })
    withNull.show()
    println(withNull.schema.treeString)
  }
}
