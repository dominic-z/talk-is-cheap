package api

import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.linalg.{DenseVector, SparseVector}
import org.junit.Test

/**
 * @author dominiczhu
 * @date 2021/3/1 下午5:35
 */
/**
 *
 * @title SparseVectorDemo
 * @author dominiczhu
 * @date 2021/3/1 下午5:35
 * @version 1.0
 */
class MLVectorDemo {

  @Test
  def vectorToString():Unit={
    val arr=Array.range(1,10).map(_.toDouble)
    val denseVec=new DenseVector(arr)
    println(denseVec.toArray.toSeq)
    println(denseVec.toString())
    println(denseVec.toSparse.toString())
    println(denseVec.toSparse.toArray.toSeq)
  }

  def vectorMerge():Unit={
    val arr=Array.range(1,10).map(_.toDouble)
    val denseVec=new DenseVector(arr)
    val sparseVec=new SparseVector(10,Array(1,2),Array(1.2,2.2))

    println(sparseVec.indices)
    println(sparseVec.values)
    println(sparseVec.size)

    new BinaryClassificationEvaluator()


  }

}
