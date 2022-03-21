package sparkReadWrite

import java.util.Properties

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/9/15 8:19 下午
 */
class SparkReadDemo {
  val conf:SparkConf = new SparkConf().setMaster("local[2]").setAppName("local")
  val sc = new SparkContext(conf)
  val spark = SparkSession.builder().config(conf).getOrCreate()
  @Test
  def sparkReadMysql()={
    //    注意，由于scala里，AnyVal的子类不包括null，即scala中的Long不能被赋值为null，因此如果下面例子里row.getLong(1)时，如果数据库里存的是null，那么会报错，而row.get(1)就不会，因为这个方法返回的是Any类型的
    val (url,usr,password)=("url","user","password")
    val props=new Properties()
    props.put("driver", "com.mysql.jdbc.Driver")
    props.put("user", usr)
    props.put("password", password)
    val df=spark.read.jdbc(url,"demo",props)
    val arr=df.take(3)

    arr.foreach(row=>{
      val canBeNull=row.get(1)
//      val exceptionIfNull=row.getLong(1)
      val canIt=row.getAs[Long]("long_num")
      println("canIt",canIt)
      println("canBeNull",canBeNull)
    })
  }
}
