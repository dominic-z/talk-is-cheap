package gettingStarted

import cases.Person
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.Test

class CreatingDatasets {
  @Test
  def createDs(): Unit = {
    val sparkConf=new SparkConf().setAppName("local").setMaster("local[2]")
    val sc=new SparkContext(sparkConf)
    val spark=SparkSession.builder().config(sparkConf).getOrCreate()
    import spark.implicits._


    val caseClassDS=Seq(Person("Andy",32)).toDS()
    caseClassDS.show()

    val primitiveDS=Seq(1,2,3).toDS()
    primitiveDS.map(_+1).collect()

    val path="people.json"
    val peopleDS=spark.read.json(path).as[Person]
    peopleDS.show()


    val peopleRDD=sc.textFile("people.txt")
    var peopleDF=peopleRDD.map(_.split(",")).map(a=>Person(a(0),a(1).trim.toInt)).toDF()

    peopleDF.createOrReplaceTempView("people")
    val teenagersDF = spark.sql("SELECT name, age FROM people WHERE age BETWEEN 13 AND 19")
    teenagersDF.map(teenager => "Name: " + teenager(0)).show()
    teenagersDF.map(teenager => "Name: " + teenager.getAs[String]("name")).show()


    implicit val mapEncoder = org.apache.spark.sql.Encoders.kryo[Map[String, Any]]
    println(teenagersDF.map(teenager => teenager.getValuesMap[Any](List("name", "age"))).collect())


    val schemaString = "name age"

    // Generate the schema based on the string of schema
    val fields = schemaString.split(" ")
      .map(fieldName => StructField(fieldName, StringType, nullable = true))
    val schema = StructType(fields)

    // Convert records of the RDD (people) to Rows
    val rowRDD = peopleRDD
      .map(_.split(","))
      .map(attributes => Row(attributes(0), attributes(1).trim))

    // Apply the schema to the RDD
    peopleDF = spark.createDataFrame(rowRDD, schema)

    // Creates a temporary view using the DataFrame
    peopleDF.createOrReplaceTempView("people")

    // SQL can be run over a temporary view created using DataFrames
    val results = spark.sql("SELECT name FROM people")

    // The results of SQL queries are DataFrames and support all the normal RDD operations
    // The columns of a row in the result can be accessed by field index or by field name
    results.map(attributes => "Name: " + attributes(0)).show()

  }

}



