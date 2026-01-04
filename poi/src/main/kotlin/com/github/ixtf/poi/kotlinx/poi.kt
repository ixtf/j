package com.github.ixtf.poi.kotlinx

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType.*
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellUtil

fun Workbook.sheet(): Sheet = firstOrNull() ?: createSheet()

fun Workbook.byteArray(): ByteArray =
  ByteArrayOutputStream().use { baos ->
    BufferedOutputStream(baos).use { write(it) }
    baos.toByteArray()
  }

fun Sheet.rowOrNull(row: Int = 0): Row? = getRow(row)

fun Sheet.row(row: Int = 0): Row = CellUtil.getRow(row, this)

fun Sheet.addMergedRegion(firstRow: Int, lastRow: Int, firstCol: Int, lastCol: Int) =
  addMergedRegion(CellRangeAddress(firstRow, lastRow, firstCol, lastCol))

fun Row.cellOrNull(col: Int = 0): Cell? = getCell(col)

fun Row.cell(col: Int = 0): Cell = CellUtil.getCell(this, col)

fun Row.cellOrNull(col: Char): Cell? = getCell(col - 'A')

fun Row.cell(col: Char): Cell = CellUtil.getCell(this, col - 'A')

fun Row.prevOrNull(count: Int = 1): Row? = sheet.getRow(rowNum - count)

fun Row.prev(count: Int = 1) = sheet.row(rowNum - count)

fun Row.nextOrNull(count: Int = 1): Row? = sheet.getRow(rowNum + count)

fun Row.next(count: Int = 1) = sheet.row(rowNum + count)

fun Cell.up(count: Int = 1): Cell = row.prev(count).cell(columnIndex)

fun Cell.down(count: Int = 1): Cell = row.next(count).cell(columnIndex)

fun Cell.left(count: Int = 1): Cell = row.cell(columnIndex - count)

fun Cell.right(count: Int = 1): Cell = row.cell(columnIndex + count)

fun Sheet.copy(sheet: Sheet, startCol: Int = 0) {
  sheet.forEach { row(it.rowNum).copy(it, startCol) }
}

fun Row.copy(row: Row, startCol: Int = 0) {
  var cell = cell(startCol)
  row.forEach {
    cell.copy(it)
    cell = cell.right()
  }
}

fun Cell.copy(cell: Cell) {
  when (cell.cellType) {
    NUMERIC -> setCellValue(cell.numericCellValue)
    STRING -> setCellValue(cell.stringCellValue)
    FORMULA -> cellFormula = cell.cellFormula
    BLANK -> Unit
    BOOLEAN -> Unit
    ERROR -> Unit
    _NONE -> Unit
  }
}

inline fun <reified T> Cell.value(): T = valueOrNull()!!

inline fun <reified T> Cell.valueOrNull(): T? {
  val value =
    if (String::class.java.isAssignableFrom(T::class.java)) {
      when (cellType) {
        BLANK,
        STRING -> stringCellValue
        NUMERIC -> numericCellValue.toString()
        else -> null
      }
    } else if (Boolean::class.java.isAssignableFrom(T::class.java)) {
      when (cellType) {
        BOOLEAN -> booleanCellValue
        else -> null
      }
    } else null
  return value as? T
}

fun Cell.isFirst(range: CellRangeAddress) =
  rowIndex == range.firstRow && columnIndex == range.firstColumn
