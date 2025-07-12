package com.github.ixtf.poi.kotlinx

import cn.hutool.core.io.FileUtil
import com.github.ixtf.core.kotlinx.extName
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

fun File.wb(): Workbook =
    if (extName().equals("xlsx", ignoreCase = true)) XSSFWorkbook(this)
    else HSSFWorkbook(POIFSFileSystem.create(this))

fun Path.wb() = toFile().wb()

fun File.write(wb: Workbook) {
  FileUtil.mkParentDirs(this)
  FileOutputStream(this).use { wb.write(it) }
}

fun Path.write(wb: Workbook) = toFile().write(wb)
