package com.simiacryptus.mindseye.art.util.view

import java.io.{ByteArrayInputStream, InputStreamReader}

import com.google.gson.GsonBuilder
import com.simiacryptus.math.{Point, Raster}
import com.simiacryptus.mindseye.lang.Layer
import com.simiacryptus.mindseye.layers.java.ImgIndexMapViewLayer

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object RotationalGroupView {

  def dist(a: Array[Double], b: Array[Double]): Double = {
    rms(a.zip(b).map(t => t._1 - t._2))
  }

  def sum(a: Array[Double], b: Array[Double]): Array[Double] = {
    a.zip(b).map(t => t._1 + t._2)
  }

  def dot(a: Array[Double], b: Array[Double]): Double = {
    a.zip(b).map(t => t._1 * t._2).sum
  }

  def unitV(doubles: Array[Double]): Array[Double] = {
    val r = rms(doubles)
    doubles.map(_ / r)
  }

  def rms(doubles: Array[Double]): Double = {
    Math.sqrt(doubles.map(Math.pow(_, 2)).sum)
  }

  def multiply(point: Array[Double], transform: Array[Array[Double]]) = {
    unitV(Array(
      point(0) * transform(0)(0) + point(1) * transform(0)(1) + point(2) * transform(0)(2),
      point(0) * transform(1)(0) + point(1) * transform(1)(1) + point(2) * transform(1)(2),
      point(0) * transform(2)(0) + point(1) * transform(2)(1) + point(2) * transform(2)(2)
    ))
  }

  val TETRAHEDRON = 10
  val OCTOHEDRON = 22
  val ICOSOHEDRON = 60

}
import com.simiacryptus.mindseye.art.util.view.RotationalGroupView._

class RotationalGroupView(x: Double, y: Double, mode: Int) extends SphericalView(x, y) {

  protected val tileProbe = Array[Double](0, 0.1, 1)
  protected val vertexSeed = Array[Double](1, 0, 0)

  lazy val primaryTile: Array[Double] = {
    val vertices = icosohedronMatrices.map(multiply(vertexSeed, _)).toArray
    val topUnique = new ArrayBuffer[Array[Double]]()
    topUnique += vertices.minBy(dot(_, tileProbe))
    while(topUnique.size < 3) topUnique += vertices.filter(v => topUnique.map(dist(v, _)).min > 1e-2).minBy(p => topUnique.map(dot(p, _)).sum)
    unitV(topUnique.reduce((a, b) => sum(a, b)).map(_ / 3))
  }

  def textureView(): Point => Point = {
    (point: Point) => {
      try {
        cartesianToSceneCoords(tileTransform(angularToCartesianCoords(canvasToAngularCoords(point))))
      } catch {
        case e : Throwable => null
      }
    }
  }


  def matrixData: String = {
    // See also https://mathandcode.com/2016/06/06/polyhedramathematica.html
    mode match {
      case TETRAHEDRON =>
        """
          |[[[-1., 0., 0.], [0., -1., 0.], [0., 0., 1.]],
          | [[-0.5, -0.5, -0.70711], [0.5, 0.5, -0.70711], [0.70711, -0.70711, 0.]],
          | [[-0.5, -0.5, 0.70711], [0.5, 0.5, 0.70711], [-0.70711, 0.70711, 0.]],
          | [[-0.5, 0.5, -0.70711], [-0.5, 0.5, 0.70711], [0.70711, 0.70711, 0.]],
          | [[-0.5, 0.5, 0.70711], [-0.5, 0.5, -0.70711], [-0.70711, -0.70711, 0.]],
          | [[0., -1., 0.], [-1., 0., 0.], [0., 0., -1.]],
          | [[0., 1., 0.], [1., 0., 0.], [0., 0., -1.]],
          | [[0.5, -0.5, -0.70711], [0.5, -0.5, 0.70711], [-0.70711, -0.70711, 0.]],
          | [[0.5, -0.5, 0.70711], [0.5, -0.5, -0.70711], [0.70711, 0.70711, 0.]],
          | [[0.5, 0.5, -0.70711], [-0.5, -0.5, -0.70711], [-0.70711, 0.70711, 0.]],
          | [[0.5, 0.5, 0.70711], [-0.5, -0.5, 0.70711], [0.70711, -0.70711, 0.]],
          | [[1., 0., 0.], [0., 1., 0.], [0., 0., 1.]]]
          |""".stripMargin
      case OCTOHEDRON =>
        """
          |[[[-1., 0., 0.], [0., -1., 0.], [0., 0., 1.]],
          | [[-1., 0., 0.], [0., 0., -1.], [0., -1., 0.]],
          | [[-1., 0., 0.], [0., 0., 1.], [0., 1., 0.]],
          | [[-1., 0., 0.], [0., 1., 0.], [0., 0., -1.]],
          | [[0., -1., 0.], [-1., 0., 0.], [0., 0., -1.]],
          | [[0., -1., 0.], [0., 0., -1.], [1., 0., 0.]],
          | [[0., -1., 0.], [0., 0., 1.], [-1., 0., 0.]],
          | [[0., -1., 0.], [1., 0., 0.], [0., 0., 1.]],
          | [[0., 0., -1.], [-1., 0., 0.], [0., 1., 0.]],
          | [[0., 0., -1.], [0., -1., 0.], [-1., 0., 0.]],
          | [[0., 0., -1.], [0., 1., 0.], [1., 0., 0.]],
          | [[0., 0., -1.], [1., 0., 0.], [0., -1., 0.]],
          | [[0., 0., 1.], [-1., 0., 0.], [0., -1., 0.]],
          | [[0., 0., 1.], [0., -1., 0.], [1., 0., 0.]],
          | [[0., 0., 1.], [0., 1., 0.], [-1., 0., 0.]],
          | [[0., 0., 1.], [1., 0., 0.], [0., 1., 0.]],
          | [[0., 1., 0.], [-1., 0., 0.], [0., 0., 1.]],
          | [[0., 1., 0.], [0., 0., -1.], [-1., 0., 0.]],
          | [[0., 1., 0.], [0., 0., 1.], [1., 0., 0.]],
          | [[0., 1., 0.], [1., 0., 0.], [0., 0., -1.]],
          | [[1., 0., 0.], [0., -1., 0.], [0., 0., -1.]],
          | [[1., 0., 0.], [0., 0., -1.], [0., 1., 0.]],
          | [[1., 0., 0.], [0., 0., 1.], [0., -1., 0.]],
          | [[1., 0., 0.], [0., 1., 0.], [0., 0., 1.]]]
          |""".stripMargin
      case ICOSOHEDRON =>
        """[[[-1., 0., 0.], [0., -1., 0.], [0., 0., 1.]],
          | [[-1., 0., 0.], [0., 1., 0.], [0., 0., -1.]],
          | [[-0.5, -0.80902, -0.30902], [-0.80902, 0.30902, 0.5], [-0.30902, 0.5, -0.80902]],
          | [[-0.5, -0.80902, -0.30902], [0.80902, -0.30902, -0.5], [0.30902, -0.5, 0.80902]],
          | [[-0.5, -0.80902, 0.30902], [-0.80902, 0.30902, -0.5], [0.30902, -0.5, -0.80902]],
          | [[-0.5, -0.80902, 0.30902], [0.80902, -0.30902, 0.5], [-0.30902, 0.5, 0.80902]],
          | [[-0.5, 0.80902, -0.30902], [-0.80902, -0.30902, 0.5], [0.30902, 0.5, 0.80902]],
          | [[-0.5, 0.80902, -0.30902], [0.80902, 0.30902, -0.5], [-0.30902, -0.5, -0.80902]],
          | [[-0.5, 0.80902, 0.30902], [-0.80902, -0.30902, -0.5], [-0.30902, -0.5, 0.80902]],
          | [[-0.5, 0.80902, 0.30902], [0.80902, 0.30902, 0.5], [0.30902, 0.5, -0.80902]],
          | [[0., -1., 0.], [0., 0., -1.], [1., 0., 0.]],
          | [[0., -1., 0.], [0., 0., 1.], [-1., 0., 0.]],
          | [[0., 0., -1.], [-1., 0., 0.], [0., 1., 0.]],
          | [[0., 0., -1.], [1., 0., 0.], [0., -1., 0.]],
          | [[0., 0., 1.], [-1., 0., 0.], [0., -1., 0.]],
          | [[0., 0., 1.], [1., 0., 0.], [0., 1., 0.]],
          | [[0., 1., 0.], [0., 0., -1.], [-1., 0., 0.]],
          | [[0., 1., 0.], [0., 0., 1.], [1., 0., 0.]],
          | [[0.5, -0.80902, -0.30902], [-0.80902, -0.30902, -0.5], [0.30902, 0.5, -0.80902]],
          | [[0.5, -0.80902, -0.30902], [0.80902, 0.30902, 0.5], [-0.30902, -0.5, 0.80902]],
          | [[0.5, -0.80902, 0.30902], [-0.80902, -0.30902, 0.5], [-0.30902, -0.5, -0.80902]],
          | [[0.5, -0.80902, 0.30902], [0.80902, 0.30902, -0.5], [0.30902, 0.5, 0.80902]],
          | [[0.5, 0.80902, -0.30902], [-0.80902, 0.30902, -0.5], [-0.30902, 0.5, 0.80902]],
          | [[0.5, 0.80902, -0.30902], [0.80902, -0.30902, 0.5], [0.30902, -0.5, -0.80902]],
          | [[0.5, 0.80902, 0.30902], [-0.80902, 0.30902, 0.5], [0.30902, -0.5, 0.80902]],
          | [[0.5, 0.80902, 0.30902], [0.80902, -0.30902, -0.5], [-0.30902, 0.5, -0.80902]],
          | [[1., 0., 0.], [0., -1., 0.], [0., 0., -1.]],
          | [[1., 0., 0.], [0., 1., 0.], [0., 0., 1.]],
          | [[-0.80902, -0.30902, -0.5], [-0.30902, -0.5, 0.80902], [-0.5, 0.80902, 0.30902]],
          | [[-0.80902, -0.30902, -0.5], [0.30902, 0.5, -0.80902], [0.5, -0.80902, -0.30902]],
          | [[-0.80902, -0.30902, 0.5], [-0.30902, -0.5, -0.80902], [0.5, -0.80902, 0.30902]],
          | [[-0.80902, -0.30902, 0.5], [0.30902, 0.5, 0.80902], [-0.5, 0.80902, -0.30902]],
          | [[-0.80902, 0.30902, -0.5], [-0.30902, 0.5, 0.80902], [0.5, 0.80902, -0.30902]],
          | [[-0.80902, 0.30902, -0.5], [0.30902, -0.5, -0.80902], [-0.5, -0.80902, 0.30902]],
          | [[-0.80902, 0.30902, 0.5], [-0.30902, 0.5, -0.80902], [-0.5, -0.80902, -0.30902]],
          | [[-0.80902, 0.30902, 0.5], [0.30902, -0.5, 0.80902], [0.5, 0.80902, 0.30902]],
          | [[-0.30902, -0.5, -0.80902], [-0.5, 0.80902, -0.30902], [0.80902, 0.30902, -0.5]],
          | [[-0.30902, -0.5, -0.80902], [0.5, -0.80902, 0.30902], [-0.80902, -0.30902, 0.5]],
          | [[-0.30902, -0.5, 0.80902], [-0.5, 0.80902, 0.30902], [-0.80902, -0.30902, -0.5]],
          | [[-0.30902, -0.5, 0.80902], [0.5, -0.80902, -0.30902], [0.80902, 0.30902, 0.5]],
          | [[-0.30902, 0.5, -0.80902], [-0.5, -0.80902, -0.30902], [-0.80902, 0.30902, 0.5]],
          | [[-0.30902, 0.5, -0.80902], [0.5, 0.80902, 0.30902], [0.80902, -0.30902, -0.5]],
          | [[-0.30902, 0.5, 0.80902], [-0.5, -0.80902, 0.30902], [0.80902, -0.30902, 0.5]],
          | [[-0.30902, 0.5, 0.80902], [0.5, 0.80902, -0.30902], [-0.80902, 0.30902, -0.5]],
          | [[0.30902, -0.5, -0.80902], [-0.5, -0.80902, 0.30902], [-0.80902, 0.30902, -0.5]],
          | [[0.30902, -0.5, -0.80902], [0.5, 0.80902, -0.30902], [0.80902, -0.30902, 0.5]],
          | [[0.30902, -0.5, 0.80902], [-0.5, -0.80902, -0.30902], [0.80902, -0.30902, -0.5]],
          | [[0.30902, -0.5, 0.80902], [0.5, 0.80902, 0.30902], [-0.80902, 0.30902, 0.5]],
          | [[0.30902, 0.5, -0.80902], [-0.5, 0.80902, 0.30902], [0.80902, 0.30902, 0.5]],
          | [[0.30902, 0.5, -0.80902], [0.5, -0.80902, -0.30902], [-0.80902, -0.30902, -0.5]],
          | [[0.30902, 0.5, 0.80902], [-0.5, 0.80902, -0.30902], [-0.80902, -0.30902, 0.5]],
          | [[0.30902, 0.5, 0.80902], [0.5, -0.80902, 0.30902], [0.80902, 0.30902, -0.5]],
          | [[0.80902, -0.30902, -0.5], [-0.30902, 0.5, -0.80902], [0.5, 0.80902, 0.30902]],
          | [[0.80902, -0.30902, -0.5], [0.30902, -0.5, 0.80902], [-0.5, -0.80902, -0.30902]],
          | [[0.80902, -0.30902, 0.5], [-0.30902, 0.5, 0.80902], [-0.5, -0.80902, 0.30902]],
          | [[0.80902, -0.30902, 0.5], [0.30902, -0.5, -0.80902], [0.5, 0.80902, -0.30902]],
          | [[0.80902, 0.30902, -0.5], [-0.30902, -0.5, -0.80902], [-0.5, 0.80902, -0.30902]],
          | [[0.80902, 0.30902, -0.5], [0.30902, 0.5, 0.80902], [0.5, -0.80902, 0.30902]],
          | [[0.80902, 0.30902, 0.5], [-0.30902, -0.5, 0.80902], [0.5, -0.80902, -0.30902]],
          | [[0.80902, 0.30902, 0.5], [0.30902, 0.5, -0.80902], [-0.5, 0.80902, 0.30902]]]
          | """
    }
  }

  lazy val icosohedronMatrices: Seq[Array[Array[Double]]] = {
    new GsonBuilder().create.fromJson(new InputStreamReader(new ByteArrayInputStream(
      matrixData.stripMargin.getBytes("UTF-8")
    )), classOf[Array[Array[Array[Double]]]]
    ).toList
  }

  override def cartisianToAngularCoords(pt:Array[Double]) = {
    super.cartisianToAngularCoords(tileTransform(pt))
  }

  def tileTransform(pt: Array[Double]) = {
    icosohedronMatrices.map(multiply(pt, _)).minBy(dot(primaryTile, _))
  }

  def tileExpansion: ImageView = (canvasDims: Array[Int]) => {
    def layer = {
      val raster: Raster = new Raster(canvasDims(0), canvasDims(1)).setFilterCircle(false )
      new ImgIndexMapViewLayer(raster, raster.buildPixelMap(tileExpansionFunction()(_)))
    }
    if(globalCache) IndexedView.cache.get((canvasDims.toList, RotationalGroupView.this)).getOrElse({
      IndexedView.cache.synchronized {
        IndexedView.cache.getOrElseUpdate((canvasDims.toList, RotationalGroupView.this), layer)
      }
    }).addRef().asInstanceOf[Layer] else layer
  }

  def tileExpansionFunction() = (point: Point) =>
    try {
      val angular1 = canvasToAngularCoords(point)
      val cartesian = angularToCartesianCoords(angular1)
      val tiled = tileTransform(cartesian)
      val angular2 = cartisianToAngularCoords(tiled)
      val result = angularToCanvasCoords(angular2)
      result
    } catch {
      case e: Throwable => null
    }

  override def canEqual(other: Any): Boolean = other.isInstanceOf[RotationalGroupView]

  override def equals(other: Any): Boolean = other match {
    case that: RotationalGroupView =>
      (that canEqual this) &&
        x == x &&
        y == y &&
        mode == mode
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(x, y, mode)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
