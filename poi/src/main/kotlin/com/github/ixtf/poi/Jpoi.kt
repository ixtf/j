package com.github.ixtf.poi

import com.github.ixtf.poi.kotlinx.wb
import java.io.File
import java.io.InputStream
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object Jpoi {
  fun wb(): Workbook = XSSFWorkbook()

  fun wb(s: String): Workbook = File(s).wb()

  fun wb(inputStream: InputStream): Workbook = XSSFWorkbook(inputStream)
}
