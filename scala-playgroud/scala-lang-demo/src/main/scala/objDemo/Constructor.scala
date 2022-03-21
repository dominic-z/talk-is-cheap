package objDemo

import java.util.{Date, UUID}

/**
 * @author dominiczhu
 * @date 2021/1/14 上午10:21
 */
/**
 *
 * @title Constructoer
 * @author dominiczhu
 * @date 2021/1/14 上午10:21
 * @version 1.0
 */
object Constructor {
  private val LOG_FILE=s"hdfs://ss-cdg-13-v2/data/MAPREDUCE/CDG/g_sng_gdt_sng_gdt_dmp_test/tdw_dominiczhu/dmp/joint_zone/rulelab/enhencement/logs/${UUID.randomUUID().toString.replace("-", "")+"-aaaa"}"


  def main(args: Array[String]): Unit = {
    println(LOG_FILE)
    println(LOG_FILE)
  }
}
