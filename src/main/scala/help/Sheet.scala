package help

import java.io.FileInputStream
import java.io.File

import org.apache.poi.xssf.usermodel._
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell

import scala.collection.mutable


class Sheet(headerSeq: Seq[String], val rows: Seq[Seq[String]]) {
  def header: Seq[String] = headerSeq
  def column(name: String): Seq[String] = {
    val idx = header.indexOf(name)
    if (idx == -1)
      throw new NoSuchElementException(s"Column $name does not exist")
    else rows.map(_(idx))
  }
  def row(index: Int): Seq[String] = rows(index)
  def filterRows(pred: Seq[String] => Boolean): Seq[Seq[String]] =
    rows.filter(pred)

  override def toString(): String = Sheet.formatTable(header +: rows)
}

object Sheet {


  def load(filepath: String): Map[String, Sheet] = {

    val workbook = new XSSFWorkbook(new FileInputStream(new File(filepath)))

    val sheetMap = mutable.Map[String, Sheet]()
    workbook.forEach { sheet =>
      val numCols = getLastNonEmptyInRow(sheet.getRow(0).cellIterator())
      val numRows = getLastNonEmptyRowStart(sheet.rowIterator()) - 1

      val headerRow = sheet.getRow(0)
      val header = for (j <- 0 until numCols) yield headerRow.getCell(j).toString

      val rows = Array.ofDim[String](numRows, numCols)

      for (i <- 0 until numRows) {
        for (j <- 0 until numCols) {
          val cell = sheet.getRow(i + 1).getCell(j)
          if (cell != null) {
            rows(i)(j) = cell.toString
          } else {
            rows(i)(j) = ""
          }
        }
      }
      sheetMap(sheet.getSheetName) = new Sheet(header, rows.map(_.toSeq).toSeq)
    }

    sheetMap.toMap
  }

  // go through first column and find last non-empty row
  @annotation.tailrec
  private def getLastNonEmptyRowStart(
      it: java.util.Iterator[Row],
      colNum: Int = 0
  ): Int = {
    if (!it.hasNext) colNum
    else {
      val row = it.next()
      val cell = row.getCell(0)
      if (cell == null || cell.toString.trim.isEmpty) colNum
      else {
        getLastNonEmptyRowStart(it, colNum + 1)
      }
    }
  }

  // go through row and find index of last non-empty cell
  @annotation.tailrec
  private def getLastNonEmptyInRow(
      it: java.util.Iterator[Cell],
      colNum: Int = 0
  ): Int = {
    if (!it.hasNext) colNum
    else {
      val cell = it.next()
      if (cell == null || cell.toString.trim.isEmpty) colNum
      else {
        getLastNonEmptyInRow(it, colNum + 1)
      }
    }
  }

  // from https://stackoverflow.com/a/55143951
  def formatTable(table: Seq[Seq[Any]]): String = {
    if (table.isEmpty) ""
    else {
      // Get column widths based on the maximum cell width in each column (+2 for a one character padding on each side)
      val colWidths = table.transpose.map(
        _.map(cell => if (cell == null) 0 else cell.toString.length).max + 2
      )
      // Format each row
      val rows = table.map(
        _.zip(colWidths)
          .map { case (item, size) => (" %-" + (size - 1) + "s").format(item) }
          .mkString("|", "|", "|")
      )
      // Formatted separator row, used to separate the header and draw table borders
      val separator = colWidths.map("-" * _).mkString("+", "+", "+")
      // Put the table together and return
      (separator +: rows.head +: separator +: rows.tail :+ separator)
        .mkString("\n", "\n", "\n")
    }
  }

}

