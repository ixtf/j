package com.gitee.ixtf.poi

import java.io.InputStream
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

object Jpoi {
  fun wb(): Workbook = XSSFWorkbook()

  fun wb(inputStream: InputStream): Workbook = XSSFWorkbook(inputStream)
}
