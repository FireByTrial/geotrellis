package geotrellis.spark.summary.polygonal

import geotrellis.spark._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.testfiles._
import geotrellis.raster.summary.polygonal._

import geotrellis.vector._

import org.scalatest.FunSpec

class SumSpec extends FunSpec
    with TestEnvironment
    with TestFiles
{

  describe("Sum Zonal Summary Operation") {
    val ones = AllOnesTestFile

    val tileLayout = ones.metadata.tileLayout
    val count = (ones.count * tileLayout.tileCols * tileLayout.tileRows).toInt
    val totalExtent = ones.metadata.extent

    it("should get correct sum over whole raster extent") {
      ones.polygonalSum(totalExtent.toPolygon) should be(count)
    }

    it("should get correct sum over a quarter of the extent") {
      val xd = totalExtent.xmax - totalExtent.xmin
      val yd = totalExtent.ymax - totalExtent.ymin

      val poly = Extent(
        totalExtent.xmin,
        totalExtent.ymin,
        totalExtent.xmin + xd / 2,
        totalExtent.ymin + yd / 2
      ).toPolygon

      val result = ones.polygonalSum(poly)
      val expected = ones.stitch.tile.polygonalSum(totalExtent, poly)

      result should be (expected)
    }

    it("should get correct sum over half of the extent in diamond shape") {
      val xd = totalExtent.xmax - totalExtent.xmin
      val yd = totalExtent.ymax - totalExtent.ymin


      val p1 = Point(totalExtent.xmin + xd / 2, totalExtent.ymax)
      val p2 = Point(totalExtent.xmax, totalExtent.ymin + yd / 2)
      val p3 = Point(totalExtent.xmin + xd / 2, totalExtent.ymin)
      val p4 = Point(totalExtent.xmin, totalExtent.ymin + yd / 2)

      val poly = Polygon(Line(Array(p1, p2, p3, p4, p1)))

      val result = ones.polygonalSum(poly)
      val expected = ones.stitch.tile.polygonalSum(totalExtent, poly)

      result should be (expected)
    }

    it("should get correct sum over polygon with hole") {
      val delta = 0.0001 // A bit of a delta to avoid floating point errors.

      val xd = totalExtent.width + delta
      val yd = totalExtent.height + delta

      val pe1 = Point(totalExtent.xmin + xd / 2, totalExtent.ymax)
      val pe2 = Point(totalExtent.xmax, totalExtent.ymin + yd / 2)
      val pe3 = Point(totalExtent.xmin + xd / 2, totalExtent.ymin)
      val pe4 = Point(totalExtent.xmin, totalExtent.ymin + yd / 2)

      val exterior = Line(Array(pe1, pe2, pe3, pe4, pe1))

      val pi1 = Point(totalExtent.xmin + xd / 2, totalExtent.ymax - yd / 4)
      val pi2 = Point(totalExtent.xmax - xd / 4, totalExtent.ymin + yd / 2)
      val pi3 = Point(totalExtent.xmin + xd / 2, totalExtent.ymin + yd / 4)
      val pi4 = Point(totalExtent.xmin + xd / 4, totalExtent.ymin + yd / 2)

      val interior = Line(Array(pi1, pi2, pi3, pi4, pi1))

      val poly = Polygon(exterior, interior)

      val result = ones.polygonalSum(poly)
      val expected = ones.stitch.tile.polygonalSum(totalExtent, poly)

      result should be (expected)
    }
  }

  describe("Sum Zonal Summary Operation (collections api)") {
    val ones = AllOnesTestFile.toCollection

    val tileLayout = ones.metadata.tileLayout
    val count = ones.length * tileLayout.tileCols * tileLayout.tileRows
    val totalExtent = ones.metadata.extent

    it("should get correct sum over whole raster extent") {
      ones.polygonalSum(totalExtent.toPolygon) should be(count)
    }

    it("should get correct sum over a quarter of the extent") {
      val xd = totalExtent.xmax - totalExtent.xmin
      val yd = totalExtent.ymax - totalExtent.ymin

      val poly = Extent(
        totalExtent.xmin,
        totalExtent.ymin,
        totalExtent.xmin + xd / 2,
        totalExtent.ymin + yd / 2
      ).toPolygon

      val result = ones.polygonalSum(poly)
      val expected = ones.stitch.tile.polygonalSum(totalExtent, poly)

      result should be (expected)
    }

    it("should get correct sum over half of the extent in diamond shape") {
      val xd = totalExtent.xmax - totalExtent.xmin
      val yd = totalExtent.ymax - totalExtent.ymin


      val p1 = Point(totalExtent.xmin + xd / 2, totalExtent.ymax)
      val p2 = Point(totalExtent.xmax, totalExtent.ymin + yd / 2)
      val p3 = Point(totalExtent.xmin + xd / 2, totalExtent.ymin)
      val p4 = Point(totalExtent.xmin, totalExtent.ymin + yd / 2)

      val poly = Polygon(Line(Array(p1, p2, p3, p4, p1)))

      val result = ones.polygonalSum(poly)
      val expected = ones.stitch.tile.polygonalSum(totalExtent, poly)

      result should be (expected)
    }

    it("should get correct sum over polygon with hole") {
      val delta = 0.0001 // A bit of a delta to avoid floating point errors.

      val xd = totalExtent.width + delta
      val yd = totalExtent.height + delta

      val pe1 = Point(totalExtent.xmin + xd / 2, totalExtent.ymax)
      val pe2 = Point(totalExtent.xmax, totalExtent.ymin + yd / 2)
      val pe3 = Point(totalExtent.xmin + xd / 2, totalExtent.ymin)
      val pe4 = Point(totalExtent.xmin, totalExtent.ymin + yd / 2)

      val exterior = Line(Array(pe1, pe2, pe3, pe4, pe1))

      val pi1 = Point(totalExtent.xmin + xd / 2, totalExtent.ymax - yd / 4)
      val pi2 = Point(totalExtent.xmax - xd / 4, totalExtent.ymin + yd / 2)
      val pi3 = Point(totalExtent.xmin + xd / 2, totalExtent.ymin + yd / 4)
      val pi4 = Point(totalExtent.xmin + xd / 4, totalExtent.ymin + yd / 2)

      val interior = Line(Array(pi1, pi2, pi3, pi4, pi1))

      val poly = Polygon(exterior, interior)

      val result = ones.polygonalSum(poly)
      val expected = ones.stitch.tile.polygonalSum(totalExtent, poly)

      result should be (expected)
    }
  }
}
