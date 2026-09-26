package help

import chisel3._
import chisel3.util._
import chisel3.experimental.requireIsChiselType

import scala.collection.immutable.ListMap

class DynamicBundle(elts: Seq[(String, Data)]) extends Record {
  override val elements = ListMap(elts.map { case (field, elt) =>
    requireIsChiselType(elt)
    field -> elt
  }: _*)
  def apply(elt: String): Data = elements(elt)
}
