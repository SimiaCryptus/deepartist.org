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

import com.simiacryptus.mindseye.art._
import com.simiacryptus.mindseye.art.models.VGG19._
import com.simiacryptus.mindseye.art.ops._
import com.simiacryptus.mindseye.eval.Trainable
import com.simiacryptus.mindseye.lang.cudnn.{MultiPrecision, Precision}
import com.simiacryptus.mindseye.lang.{Layer, Tensor}
import com.simiacryptus.mindseye.layers.java.SumInputsLayer
import com.simiacryptus.mindseye.network.PipelineNetwork
import com.simiacryptus.notebook.NotebookOutput
import com.simiacryptus.ref.lang.RefUtil

object VisualStyleNetwork {
  def DOMELA_1(implicit log: NotebookOutput) = new VisualStyleNetwork(
    styleLayers = List(
      VGG19_1c1,
      VGG19_1c2,
      VGG19_1c3,
      VGG19_1c4
    ),
    styleModifiers = List(
      new ChannelMeanMatcher(),
      new GramMatrixEnhancer().setTileSize(400)
    ),
    styleUrl = ArtUtil.findFiles("cesar-domela")
  )

  def MONET_1(implicit log: NotebookOutput) = new VisualStyleNetwork(
    styleLayers = List(VGG19_0b,
      VGG19_1a,
      VGG19_1b2,
      VGG19_1c4,
      VGG19_1d4,
      VGG19_1e4),
    styleModifiers = List(
      //      new ChannelMeanMatcher(),
      //      new GramMatrixMatcher().setTileSize(400),
      new MomentMatcher().setTileSize(400),
      new GramMatrixEnhancer().setMinMax(-.25, .25).setTileSize(400)
    ),
    styleUrl = ArtUtil.findFiles("claude-monet")
  )

  def MANET_1(implicit log: NotebookOutput) = new VisualStyleNetwork(
    styleLayers = List(VGG19_0b,
      VGG19_1b1,
      VGG19_1b2),
    styleModifiers = List(
      new GramMatrixMatcher().setTileSize(400),
      new GramMatrixEnhancer().setTileSize(400)
    ),
    styleUrl = ArtUtil.findFiles("edouard-manet")
  )

  def DRAWING_1(implicit log: NotebookOutput) = new VisualStyleNetwork(
    styleLayers = List(VGG19_1a, VGG19_1b1, VGG19_1c1, VGG19_1d1, VGG19_1e1),
    styleModifiers = List(
      new ChannelMeanMatcher(),
      new GramMatrixMatcher(),
      new GramMatrixEnhancer().setMinMax(-0.25, 0.25).setTileSize(400)
    ),
    styleUrl = ArtUtil.findFiles(
      """preliminaries-the-alpha-the-maiestas-domini-and-the-portraits-of-the-authors.jpg!Large.jpg
        |            |mouvement-1989.jpg!Large.jpg
        |            |hartley-ginny-1970.jpg!Large.jpg
        |            |not_detected_233129.jpg!Large.jpg
        |            |illustration-from-the-twelve-hours-of-the-green-houses-c-1795-colour-woodblock-print.jpg!Large.jpg
        |            |the-kabuki-actors-ichikawa-danjuro-vii-as-iwafuji-1824.jpg!Large.jpg
        |            |metamorphosis-iii-1968-2.jpg!Large.jpg
        |            |plum-1930.jpg!Large.jpg
        |            |birch-trees-1911.jpg!Large.jpg
        |            |d-landscape-1932.jpg!Large.jpg
        |            |royal-flush-1977.jpg!Large.jpg
        |            |pupppet-theatre-1907.jpg!Large.jpg
        |            |a-barbet-himalayan-blue-throated-bird-1615.jpg!Large.jpg
        |            |untitled-1899.jpg!Large.jpg
        |            |walk-of-louis-xv-in-childhood.jpg!Large.jpg
        |            |negro-attacked-by-a-jaguar-1910.jpg!Large.jpg
        |            |design-for-tulip-and-willow-indigo-discharge-wood-block-printed-fabric-1873.jpg!Large.jpg
        |            |job-1896.jpg!Large.jpg
        |            |spring-motif-view-from-stone-island-to-krestovsky-and-yelagin-regions-1904.jpg!Large.jpg
        |            |girl-with-a-rose-in-her-lap-1960.jpg!Large.jpg
        |            |untitled-1949.jpg!Large.jpg
        |            |the-dragon.jpg!Large.jpg
        |            |king-and-his-subjects-2005.jpg!Large.jpg""".stripMargin.split('\n').toSet
    )
  )

  def PAINTING_1(implicit log: NotebookOutput) = new VisualStyleNetwork(
    styleLayers = List(VGG19_1a, VGG19_1b1, VGG19_1c1, VGG19_1d1, VGG19_1e1),
    styleModifiers = List(
      new ChannelMeanMatcher(),
      new GramMatrixMatcher(),
      new GramMatrixEnhancer().setMinMax(-0.25, 0.25).setTileSize(400)
    ),
    styleUrl = ArtUtil.findFiles(
      """tall-portuguese-woman-1916.jpg!Large.jpg
        |angel-of-the-last-judgment-1911.jpg!Large.jpg
        |music-1904.jpg!Large.jpg
        |wedding-ornaments-2005.jpg!Large.jpg
        |a-young-man-breaking-into-the-girls-dance-and-the-old-women-are-in-panic.jpg!Large.jpg
        |harlequin-and-clown-with-mask-1942.jpg!Large.jpg
        |the-street-enters-the-house-1911-1.jpg!Large.jpg
        |sc-ne-de-cirque.jpg!Large.jpg
        |conversation-two-nudes-in-an-interior-1978.jpg!Large.jpg
        |abstract-composition-1955.jpg!Large.jpg
        |the-two-models-1930.jpg!Large.jpg
        |composition-no-62-1917.jpg!Large.jpg
        |nebozvon-skybell-1919.jpg!Large.jpg
        |portrait-of-walter-lippman.jpg!Large.jpg
        |goya-s-lover-1977.jpg!Large.jpg
        |the-muses.jpg!Large.jpg
        |at-olympia-s-design-for-tales-of-hoffmann-by-j-offenbach-1915.jpg!Large.jpg
        |thalys-2009.jpg!Large.jpg
        |royal-flush-1977.jpg!Large.jpg
        |duel-1912.jpg!Large.jpg""".stripMargin.split('\n').toSet
    )
  )


  def pixels(canvas: Tensor) = {
    if (null == canvas) 0 else {
      val dimensions = canvas.getDimensions
      canvas.freeRef()
      val pixels = dimensions(0) * dimensions(1)
      pixels
    }
  }

}

case class VisualStyleNetwork
(
  styleLayers: Seq[VisionPipelineLayer] = Seq.empty,
  styleModifiers: Seq[VisualModifier] = Seq.empty,
  styleUrl: Seq[String] = Seq.empty,
  styleUrls: Seq[String] = Seq.empty,
  precision: Precision = Precision.Float,
  viewLayer: Seq[Int] => List[Layer] = _ => List(new PipelineNetwork(1)),
  filterStyleInput: Boolean = true,
  override val tileSize: Int = 1400,
  override val tilePadding: Int = 64,
  override val minWidth: Int = 1,
  override val maxWidth: Int = 10000,
  override val maxPixels: Double = 5e8,
  override val magnification: Seq[Double] = Array(1.0)
)(implicit override val log: NotebookOutput) extends ImageSource(styleUrl, styleUrls) with VisualNetwork {

  def apply(canvas: Tensor, content: Tensor): Trainable = {
    val dimensions = content.getDimensions
    content.freeRef()
    apply(canvas, dimensions)
  }

  def apply(canvas: Tensor, dimensions: Array[Int]) = {
    val loadedImages = loadImages(dimensions.reduce(_*_))
    try {
      val contentDimensions = dimensions
      new SumTrainable((for (
        pipelineLayers <- styleLayers.groupBy(_.getPipelineName).values
      ) yield {
        var styleNetwork: PipelineNetwork = null
        if(!filterStyleInput) styleNetwork = SumInputsLayer.combine(pipelineLayers.filter(x => styleLayers.contains(x)).map(pipelineLayer => {
          val network = styleModifiers.reduce(_ combine _).build(pipelineLayer, contentDimensions, (x:Tensor)=>x, RefUtil.addRef(loadedImages): _*)
          network.freeze()
          network
        }): _*)
        for(layer <- viewLayer(contentDimensions)) yield {
          if(filterStyleInput) styleNetwork = SumInputsLayer.combine(pipelineLayers.filter(x => styleLayers.contains(x)).map(pipelineLayer => {
            val network = styleModifiers.reduce(_ combine _).build(pipelineLayer, contentDimensions, layer.asTensorFunction(), RefUtil.addRef(loadedImages): _*)
            network.freeze()
            network
          }): _*)
          new TiledTrainable(canvas.addRef(), layer, tileSize, tilePadding, precision) {

            override def getLayer(): Layer = {
              styleNetwork.addRef()
            }

            override protected def getNetwork(regionSelector: Layer): PipelineNetwork = {
              regionSelector.freeRef()
              val network = styleNetwork.addRef()
              MultiPrecision.setPrecision(network.addRef(), precision)
              network
            }

            override def _free(): Unit = {
              styleNetwork.freeRef()
              super._free()
            }
          }
        }
      }).flatten.toArray: _*)
    } finally {
      RefUtil.freeRef(loadedImages)
      canvas.freeRef()
    }
  }

  def withContent(
                   contentLayers: Seq[VisionPipelineLayer],
                   contentModifiers: Seq[VisualModifier] = List(new ContentMatcher)
                 ) = VisualStyleContentNetwork(
    styleLayers = styleLayers,
    styleModifiers = styleModifiers,
    styleUrl = styleUrl,
    styleUrls = styleUrls,
    precision = precision,
    viewLayer = viewLayer,
    contentLayers = contentLayers,
    contentModifiers = contentModifiers,
    tilePadding = tilePadding,
    tileSize = tileSize,
    minWidth = minWidth,
    maxWidth = maxWidth,
    maxPixels = maxPixels,
    magnification = magnification
  )
}
