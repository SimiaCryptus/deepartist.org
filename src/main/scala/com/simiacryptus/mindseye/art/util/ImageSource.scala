/*
 * Copyright (c) 2019 by Andrew Charneski.
 *
 * The author licenses this file to you under the
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.simiacryptus.mindseye.art.util

import com.simiacryptus.mindseye.lang.Tensor
import com.simiacryptus.mindseye.util.ImageUtil
import com.simiacryptus.notebook.NotebookOutput

import scala.collection.JavaConverters._
import scala.util.Random

object ImageSource {

}

class ImageSource(urls: Seq[String], urls2: Seq[String] = Seq.empty)(implicit val log: NotebookOutput) {
  def tileSize: Int = 400

  def tilePadding: Int = 16

  def loadImages(canvasPixels: Int): Array[Tensor] = {
    val images = if (!urls2.isEmpty) {
      Random.shuffle(ImageArtUtil.loadImages(log, urls2.asJava, -1).toList)
    } else {
      Random.shuffle(urls.toList)
        .map(ImageArtUtil.loadImage(log, _, -1))
    }
    val styles = images
      .flatMap(styleImage => {
        magnification.map(magnification=>{
          val stylePixels = styleImage.getWidth * styleImage.getHeight
          var finalWidth = if (canvasPixels > 0) (styleImage.getWidth * Math.sqrt((canvasPixels.toDouble / stylePixels) * magnification)).toInt else -1
          if (finalWidth < minWidth && finalWidth > 0) finalWidth = minWidth
          if (finalWidth > Math.min(maxWidth, styleImage.getWidth)) finalWidth = Math.min(maxWidth, styleImage.getWidth)
          val resized = ImageUtil.resize(styleImage, finalWidth, true)
          Tensor.fromRGB(resized)
        })
      }).toBuffer
    require(!styles.isEmpty)
    while (styles.map(_.getDimensions).map(d => d(0) * d(1)).sum > maxPixels) styles.remove(0)
    require(!styles.isEmpty)
    styles.toArray
  }

  def minWidth: Int = 1

  def maxWidth: Int = 10000

  def maxPixels: Double = 5e7

  def magnification: Seq[Double] = Array(1.0)

}
