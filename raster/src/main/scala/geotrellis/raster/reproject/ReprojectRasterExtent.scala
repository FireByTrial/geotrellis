/*
 * Copyright 2016 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.raster.reproject

import geotrellis.raster._
import geotrellis.vector._
import geotrellis.proj4._


import spire.math.Integral


object ReprojectRasterExtent {
  import Reproject.Options
  /*
   * This function is used to suggest the extent and resolution
   * appropriate given the indicated reprojection CRSs.  It walks
   * the edges of the input file (approximately 20 sample points along each
   * edge) transforming into output coordinates in order to get an extents box.
   */
  def reprojectExtent(ge: GridExtent, transform: Transform): Extent = {
    val extent = ge.extent
    val (cols, rows) = (extent.width / ge.cellwidth, extent.height / ge.cellheight)
    val PIXEL_STEP = math.min(50.0, math.min(cols, rows)).toInt

    // Find the threshold to densify the extent at.
    val xThreshold = (cols / PIXEL_STEP) * ge.cellwidth
    val yThreshold = (rows / PIXEL_STEP) * ge.cellheight
    val threshold = math.min(xThreshold, yThreshold)

    // Densify the extent to get a more accurate reprojection
    val denseGeom = Polygon(Densifier.densify(extent.toPolygon.jtsGeom, threshold).asInstanceOf[org.locationtech.jts.geom.Polygon])
    denseGeom.reproject(transform).envelope
  }

  def apply(ge: GridExtent, transform: Transform, options: Options): GridExtent = {
    val extent = ge.extent
    val newExtent = reprojectExtent(ge, transform)

    val (transformedXmin, transformedYmax) = {
      transform(extent.xmin, extent.ymax)
    }

    val (transformedXmax, transformedYmin) = {
      transform(extent.xmax, extent.ymin)
    }

    options.parentGridExtent match {
      case Some(parentGridExtent) =>
        parentGridExtent.createAlignedGridExtent(Extent(transformedXmin, transformedYmin, transformedXmax, transformedYmax))
      case None =>
        val (pixelSizeX, pixelSizeY) =
          options.targetCellSize match {
            case Some(cellSize) =>
              (cellSize.width, cellSize.height)
            case None =>
              val distance = (transformedXmin, transformedYmax).distance((transformedXmax, transformedYmin))
              val cols = ge.extent.width / ge.cellwidth
              val rows = ge.extent.height / ge.cellheight
              val pixelSize = distance / math.sqrt(cols * cols + rows * rows)
              (pixelSize, pixelSize)
          }

        val newColsDouble = newExtent.width / pixelSizeX
        val newRowsDouble = newExtent.height / pixelSizeY

        val newCols = (newColsDouble + 0.5).toInt//.toLong
        val newRows = (newRowsDouble + 0.5).toInt//.toLong

        //Adjust the extent to match the pixel size.
        val adjustedExtent = Extent(newExtent.xmin, newExtent.ymax - (pixelSizeY*newRows), newExtent.xmin + (pixelSizeX*newCols), newExtent.ymax)

        // TODO: consider adding .withExtent and .withCellSize to GridExtent so we can parametrize this function on N, T <: GridExtent[N] and keep the type T
        // ^ this would also remove the requirement of : Integral on N
        new GridExtent[N](adjustedExtent, CellSize(pixelSizeX, pixelSizeY))
    }
  }

  def apply[N: Integral](ge: GridExtent[N], transform: Transform): GridExtent[N] =
    apply(ge, transform, Options.DEFAULT)

  def apply[N: Integral](ge: GridExtent[N], src: CRS, dest: CRS, options: Options): GridExtent[N] =
    if(src == dest) {
      ge
    } else {
      apply(ge, Transform(src, dest), options)
    }

  def apply[N: Integral](ge: GridExtent[N], src: CRS, dest: CRS): GridExtent[N] =
    apply(ge, src, dest, Options.DEFAULT)

  def apply(re: RasterExtent, transform: Transform, options: Reproject.Options): RasterExtent =
    apply(re: GridExtent[Int], transform, options).toRasterExtent

  def apply(re: RasterExtent, transform: Transform): RasterExtent =
    apply(re, transform, Options.DEFAULT)

  def apply(re: RasterExtent, src: CRS, dest: CRS, options: Options): RasterExtent =
    if(src == dest) {
      re
    } else {
      apply(re, Transform(src, dest), options)
    }

  def apply(re: RasterExtent, src: CRS, dest: CRS): RasterExtent =
    apply(re, src, dest, Options.DEFAULT)

}
