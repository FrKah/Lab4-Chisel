
import help._

import chisel3._
import chisel3.util._

case class Field(name: String, typ: String, msb: Int, lsb: Int, init: String) {
  // number of bits this field occupies, e.g. msb=11, lsb=0 -> 12 bits
  def width: Int = msb - lsb + 1
}

case class Register(name: String, offset: BigInt, fields: Seq[Field])

object CsrParsing {
  // Parse a numeric cell value, as found in Offset/Base Address/Init columns.
  //
  // hex text is parsed as hex; anything else is parsed as plain decimal integer
  def parseHex(s: String): BigInt =
    if (s.startsWith("0x")) BigInt(s.stripPrefix("0x"), 16)
    else BigInt(s.takeWhile(_ != '.'))

  // build the flat block_reg_field (or block_reg when the field is unnamed) signal name
  def flatName(instName: String, regName: String, fieldName: String): String =
    if (fieldName.isEmpty) s"${instName}_${regName}" else s"${instName}_${regName}_${fieldName}"

  def parseRegisters(sheet: Sheet): Seq[Register] = {
    // get all unique register names in a given block
    val uniqueNames = sheet.column("Register").distinct
    // loop through all unique register names building a sequence of register objects
    for (name <- uniqueNames) yield {
      // get the rows in the spreadsheet corresponding to the register name
      val rowsForReg = sheet.filterRows(row => row(0) == name)
      // get the offset from the first element of the row list because all rows for one register correspond
      // to the same offset are the offset is unique to the register not the rows (each register as one offset)
      val offset = parseHex(rowsForReg.head(1))
      // build the field instances sequence using the extracted rows
      val fields = for (row <- rowsForReg) yield {
        val Array(msbStr, lsbStr) = row(4).split(":")
        Field(row(2), row(3), msbStr.toInt, lsbStr.toInt, row(5))
      }
      // build the register instance
      Register(name, offset, fields)
    }
  }
}
import CsrParsing._

class ApbPort extends Bundle {
  val psel = Input(Bool())
  val penable = Input(Bool())
  val pwrite = Input(Bool())
  val paddr = Input(UInt(32.W))
  val pwdata = Input(UInt(32.W))
  val prdata = Output(UInt(32.W))
  val pready = Output(Bool())
  val pslverr = Output(Bool())
}

class CsrAdapter(descriptionSheetPath: String) extends Module {

  val sheets = Sheet.load(descriptionSheetPath)
  val map = sheets("Map")
  println(map)

  val apb = IO(new ApbPort)
  
  // Parse each distinct block type's sheet exactly once. Without this,
  // two instances of the same block type (soc.xlsx has gpio0 and gpio1,
  // both "Gpio") would each independently re-parse that sheet into an
  // identical Seq[Register], since the layout only depends on blockType,
  // never on the instance.
  val registersByBlockType: Map[String, Seq[Register]] =
    map.column("Block").distinct.map(blockType => blockType -> parseRegisters(sheets(blockType))).toMap

  // Resolve every register to its absolute address once here, and
  // pre-compute each field's flat name once too (paired up as
  // (name, field)) - so nothing downstream (csrTable, or the address
  // decode further down) ever needs to re-derive an address or call
  // flatName(...) again for the same field.
  val registerTable: Seq[(BigInt, Seq[(String, Field)])] = for {
    row <- map.rows
    blockType = row(0)
    instName = row(1)
    baseAddr = parseHex(row(3))
    register <- registersByBlockType(blockType)
  } yield {
    val addr = baseAddr + register.offset
    val namedFields = register.fields.map(field => flatName(instName, register.name, field.name) -> field)
    (addr, namedFields)
  }

  // Flatten registerTable down to one entry per field: used to build the
  // csr IO bundle and the backing registers below.
  val csrTable = for {
    (addr, namedFields) <- registerTable
    (name, field) <- namedFields
  } yield (addr, name, field)

  // Build the CSR IO bundle: one or two ports per field, depending on its type
  // because rw/ro get one port, wotrg/rotrg get a data + trg pair and const gets no port at all)
  // map gives one Seq[(String,Data)] per field (0, 1 or 2 entries); flatten
  // merges those into the single flat list DynamicBundle needs.
  val csrIOs: Seq[(String, Data)] = csrTable.map { case (_, name, field) =>
    field.typ match {
      case "rw" => Seq(name -> Output(UInt(field.width.W)))
      case "ro" => Seq(name -> Input(UInt(field.width.W)))
      case "wotrg" => Seq(s"${name}_data" -> Output(UInt(field.width.W)), s"${name}_trg" -> Output(Bool()))
      case "rotrg" => Seq(s"${name}_data" -> Input(UInt(field.width.W)), s"${name}_trg" -> Output(Bool()))
      case "const" => Seq()
    }
  }.flatten
  val csr = IO(new DynamicBundle(csrIOs))

  // Filters csrTable down to just the software-writable fields (rw, wotrg), 
  // and for each builds a RegInit/Reg (reset to its Init value, or left uninitialized if "?"), 
  // collected into a Map[String, UInt] keyed by flat name : this is the actual hardware state backing those fields.
  val csrRegs: Map[String, UInt] = csrTable
    .filter { case (_, _, field) => field.typ == "rw" || field.typ == "wotrg" }
    .map { case (_, name, field) =>
      val reg =
        if (field.init == "?") Reg(UInt(field.width.W))
        else RegInit(parseHex(field.init).U(field.width.W))
      name -> reg
    }
    .toMap

  // Drive the always-on parts of the csr IO: rw/wotrg outputs always reflect
  // their backing register. trg outputs are set to 0 here every cycle by
  // default; the address decode below overrides a trg to 1 for one cycle,
  // only when that field's address is actually accessed.
  for ((_, name, field) <- csrTable) {
    field.typ match {
      case "rw" =>
        csr(name) := csrRegs(name)
      case "wotrg" =>
        csr(s"${name}_data") := csrRegs(name)
        csr(s"${name}_trg") := false.B
      case "rotrg" =>
        csr(s"${name}_trg") := false.B
      case _ => // ro, const: nothing to drive from this side
    }
  }

  // APB handshake: psel (+pwrite/!pwrite) sets wrAccess/rdAccess high one
  // cycle later - that high cycle is the access phase - then it clears
  // itself the cycle after, so each stays high for exactly one cycle
  // (a "pulse") per transaction, with no wait states.
  val wrAccess = RegInit(false.B)
  val rdAccess = RegInit(false.B)
  when(wrAccess) {
    wrAccess := false.B
  }.elsewhen(apb.psel && apb.pwrite) {
    wrAccess := true.B
  }
  when(rdAccess) {
    rdAccess := false.B
  }.elsewhen(apb.psel && !apb.pwrite) {
    rdAccess := true.B
  }
  apb.pready := wrAccess || rdAccess

  // Defaults: no data, error unless a matching address clears it below.
  apb.prdata := 0.U
  apb.pslverr := true.B

  // Address decode: one when(paddr === addr) per register, combining all
  // of that register's fields (a register can mix field types). Reuses
  // registerTable built above instead of re-resolving addresses.
  for ((addr, namedFields) <- registerTable) {
    when(apb.paddr === addr.U) {
      // wotrg fields have no read path; everything else does
      val readableFields = namedFields.filter { case (_, field) => field.typ != "wotrg" }
      // only rw/wotrg fields can be written
      val writableFields = namedFields.filter { case (_, field) => field.typ == "rw" || field.typ == "wotrg" }

      // Read: OR every readable field, shifted into its bit position,
      // into one word with reduce, then assign prdata once
      when(rdAccess) {
        if (readableFields.nonEmpty) {
          apb.pslverr := false.B
          val regVal = readableFields.map { case (name, field) =>
            val fieldVal: UInt = field.typ match {
              case "rw"    => csrRegs(name)
              case "ro"    => csr(name).asUInt
              case "rotrg" => csr(s"${name}_data").asUInt
              case "const" => parseHex(field.init).U(field.width.W)
            }
            fieldVal << field.lsb
          }.reduce(_ | _)
          apb.prdata := regVal
          for ((name, field) <- readableFields if field.typ == "rotrg") {
            csr(s"${name}_trg") := true.B
          }
        }
      // Write: store each writable field from its slice of pwdata.
      }.elsewhen(wrAccess) {
        if (writableFields.nonEmpty) {
          apb.pslverr := false.B
          for ((name, field) <- writableFields) {
            csrRegs(name) := apb.pwdata(field.msb, field.lsb)
            if (field.typ == "wotrg") csr(s"${name}_trg") := true.B
          }
        }
      }
    }
  }

}

object CsrAdapter extends App {
  emitVerilog(
    new CsrAdapter("soc.xlsx"),
    Array("--target-dir", "generated")
  )
}