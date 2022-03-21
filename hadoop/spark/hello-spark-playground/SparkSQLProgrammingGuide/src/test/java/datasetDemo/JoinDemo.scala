package datasetDemo

import cases.{Person, Student, StudentScore}
import gettingStarted.Employee
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.Test


/**
 * @author dominiczhu
 * @date 2020/8/26 3:49 下午
 */
class JoinDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc: SparkContext = new SparkContext(sparkConf)
  val spark: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()

  import spark.implicits._

  def main(args: Array[String]): Unit = {

    simpleDSJoin
    //    casesDfJoin
    //    nestedCasesDfAndSimpleDfJoin
    //    nullJoin
  }

  /**
   * 演示最简单的join
   *
   */
  @Test
  def simpleDSJoin(): Unit = {

    var leftDS = Seq(Person("Andy", 32), Person("Mike", 32)).toDS()

    var rightDS = Seq(Student("Andy", 32)).toDS()

    //    多列进行join
    val resDf = leftDS.join(rightDS, $"name" <=> $"stuName" && $"age" <=> $"stuAge")
    resDf.show()

    //    下面是如果列名相同的情况下进行join，通过自己join来演示，这种情况会出现两个name列
    var joinWithTheSameName = leftDS.join(leftDS, leftDS("name") === leftDS("name"))
    joinWithTheSameName.show()

    // leftjoin会报错
    joinWithTheSameName = leftDS.join(leftDS.drop("age"), col("name"),"left")
    joinWithTheSameName.show()

    //    下面这种情况会出现两个name列
    joinWithTheSameName = leftDS.as("left").join(leftDS.as("right"), $"left.name".as("leftName") <=> $"right.name")
    joinWithTheSameName.show() //不好使，结果的列头仍然是两个name两个age

    //    joinWithTheSameName.select("name").show()// 会报错的，因为这里有多个列都叫name 解决方法见博客，修改方法见下一行，加一个left就好
    joinWithTheSameName.select($"left.name", $"right.name".as("rightName"), $"right.age").show() //好使，说明这个ds里是可以区分两个name的

    joinWithTheSameName = leftDS.as("left").join(leftDS.as("right"), Seq("name"))
    joinWithTheSameName.show() //只剩下一个name了，但还是会有两个age，但这个不重要

    joinWithTheSameName = leftDS.as("left").join(leftDS.as("right"), "name")
    joinWithTheSameName.show()

    val rightEmpDS = Seq(Employee("Andy", 32)).toDS()
    leftDS.join(rightEmpDS,Seq("name")).show()
    rightEmpDS.join(leftDS,Seq("name")).show()//观察顺序可以发现，join的列会在最前面，然后是左表然后是右表
    println("")

    leftDS = Seq(Person("Andy", 32), Person("Mike", 32)).toDS()

    rightDS = Seq(Student("Andy", 32),Student("Andy", 50)).toDS()
    leftDS.join(rightDS, $"name" <=> $"stuName").show()
  }

  @Test
  def joinWithDifferentType():Unit={
    /*
    * 类型不同也可以join
    * */
    val leftDS = Seq(Person("32", 1), Person("12", 3)).toDS()

    val rightDS = Seq(Student("Andy", 32), Student(null, 12)).toDS()
    val resDf = leftDS.join(rightDS, $"name" <=> $"stuAge")
    resDf.show()
  }

  @Test
  def joinWithEmptyDs():Unit={
    val leftDS = Seq(Person("32", 1), Person("12", 3)).filter(_.age>10).toDS()

    val rightDS = Seq(Student("Andy", 32), Student(null, 12)).toDS()
    val resDf = leftDS.join(rightDS, $"name" <=> $"stuName","left")
    resDf.show()
  }

  /**
   * 演示如果有列为null的情况下会不会join成功
   *
   */
  @Test
  def nullInnerJoin(): Unit = {
    val leftDS = Seq(Person("Andy", 32), Person("Mike", 21), Person(null, 32),Person("John",44)).toDS()

    val rightDS = Seq(Student("Andy", 32), Student(null, 35)).toDS()
    val withNull = leftDS.join(rightDS, $"name" <=> $"stuName")
    withNull.show()

    val ignoreNull = leftDS.join(rightDS, $"name" === $"stuName")// 忽略了null
    ignoreNull.show()

  }

  /**
   * 演示如果有列为null的情况下会不会join成功
   *
   */
  @Test
  def nullLeftJoin(): Unit = {
    val leftPersonDS = Seq(Person("Andy", 32), Person("Mike", 21), Person(null, 32),Person("John",44)).toDS()
    val rightPersonDS = Seq(Person("Andy", 32), Person(null, 32),Person("Frady",44)).toDS()
    val rightStuDS = Seq(Student("Andy", 32), Student(null, 35),Student("Frady", 35)).toDS()

    val joinStuWithNull = leftPersonDS.join(rightStuDS, $"name" <=> $"stuName","left")
    joinStuWithNull.show()


    // leftJoin之中，leftDs的列保留，但是join的两个列之中，两列的null值不会视作相等
    val joinStuIgnoreNull = leftPersonDS.join(rightStuDS, $"name" === $"stuName","left")
    joinStuIgnoreNull.show()

    // 同名列join的时候，执行的方式等同于===方式
    val joinPersonWithNull = leftPersonDS.join(rightPersonDS, Seq("name"),"left")
    joinPersonWithNull.show()

  }

  @Test
  def casesDfJoin(): Unit = {


    val leftDS = Seq(StudentScore(Student("mike", 19), 89), StudentScore(Student("amy", 18), 85)).toDS()
    val rightDS = Seq(StudentScore(Student("mike", 19), 89), StudentScore(Student("tom", 20), 87)).toDS()

    leftDS.show()
    rightDS.show()
    val resDf = leftDS.join(rightDS, Seq("student", "score"))
    resDf.show()
  }

  @Test
  def nestedCasesDfAndSimpleDfJoin(): Unit = {
    val leftDS = Seq(StudentScore(Student("mike", 19), 89), StudentScore(Student("amy", 18), 85)).toDS()
    leftDS.show()
    val rightRdd = sc.parallelize(Seq(("mike", 19, 95))) // tuple

    val rightDs = spark.createDataset(rightRdd)
    rightDs.show()
    val rightDf = rightDs.withColumnRenamed("_1", "right_name").withColumnRenamed("_2", "right_age").withColumnRenamed("_3", "right_score")
    rightDf.show()

    //

    //    leftDS.show()
    //    rightDS.show()
    //    val resDf = leftDS.join(rightDS, Seq("student","score"))
    //    resDf.show()
  }

  @Test
  def multiConditionJoin():Unit={

    val leftDF = Seq(("mike", "male", 89),("mike", null, 89), ("andy", "male", 89),("andy", "female", 89))
      .toDF("l_name","l_gender","l_score")
    val rightDF = Seq(("mike", "male", "xa"), ("andy", null, "sz")).toDF("name","gender","city")

    leftDF.join(rightDF,(col("l_name")===col("name") and col("l_gender")===col("gender")) or (col("l_name")===col("name")))
      .show()


  }

}


