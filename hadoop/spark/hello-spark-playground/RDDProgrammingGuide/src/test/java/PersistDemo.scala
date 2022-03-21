import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2020/9/19 10:12 上午
 */
class PersistDemo {
  val sparkConf: SparkConf = new SparkConf().setAppName("local").setMaster("local[2]")
  val sc = new SparkContext(sparkConf)
  @Test
  def persistDemo()={
    val rdd=sc.range(1,10)

    val rdd1=rdd.map(i=>{
      i*10
    })

    val action1=rdd1.reduce((i1,i2)=>i1+i2)
    val action2=rdd1.reduce((i1,i2)=>i1*i2)
    // 上面这种情况，rdd1屁股后面跟了两种reduce，那么rdd1的这个map实际上会执行两次，那么如何persist呢


    val rdd2=rdd.map(i=>{
      i*10
    }).persist(StorageLevel.MEMORY_AND_DISK_SER)
    val actionP1=rdd2.reduce((i1,i2)=>i1+i2)
    val actionP2=rdd2.reduce((i1,i2)=>i1*i2)
    //由于persit的存在，rdd.map的这个结果会持久化，当第二次调用reduce计算actionP2的时候，rdd2的这个map不会再执行一次了

  }

}
