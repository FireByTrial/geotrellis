package geotrellis.spark.io

import geotrellis.spark.LayerId
import org.apache.hadoop.hbase.{HColumnDescriptor, TableName}
import org.apache.hadoop.hbase.client.Table
import org.apache.hadoop.hbase.util.Bytes

package object hbase {
  implicit def stringToTableName(str: String): TableName = TableName.valueOf(str)
  implicit def stringToBytes(str: String): Array[Byte]  = Bytes.toBytes(str)
  implicit def intToBytes(i: Int): Array[Byte] = Bytes.toBytes(i)
  implicit def longToBytes(l: Long): Array[Byte] = Bytes.toBytes(l)

  def columnFamily(id: LayerId) = s"${id.name}:${id.zoom}"
}
