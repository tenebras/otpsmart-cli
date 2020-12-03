package com.github.tenebras.otpclient

import com.github.tenebras.otpclient.model.Sheet
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import java.io.File

class XlsxParser {
    fun parse(file: File): List<Sheet> {
        val book = HSSFWorkbook(file.inputStream())
        val response = mutableListOf<Sheet>()

        for (sheetIdx in 0 until book.numberOfSheets) {

            val sheet = book.getSheetAt(sheetIdx)

            if (sheet.physicalNumberOfRows == 0) {
                response.add(Sheet(sheet.sheetName, emptyList()))
            } else {
                val rows = mutableListOf<Map<String, Any?>>()
                val firstRow = sheet.getRow(sheet.firstRowNum)

                for (rowIdx in (sheet.firstRowNum + 1)..sheet.lastRowNum) {
                    val row = sheet.getRow(rowIdx) ?: continue

                    val cells = mutableMapOf<String, Any?>()
                    var hasContent = false

                    for (cellIdx in firstRow.firstCellNum until firstRow.lastCellNum) {
                        try {
                            val cell = row.getCell(cellIdx)
                            val key = firstRow.getCell(cellIdx)?.stringCellValue ?: ""

                            cells[key] = cell.value()

                            hasContent = hasContent || cells[key] != null
                        } catch (e: Throwable) {
                            println(e)
                        }
                    }

                    if (hasContent) {
                        rows.add(cells)
                    }
                }

                response.add(Sheet(sheet.sheetName, rows))
            }
        }

        book.close()

        return response
    }

    private fun HSSFCell.value(): Any? {
        return when (cellType) {
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(this)) {
                    dateCellValue
                } else {
                    numericCellValue
                }
            }
            CellType.STRING -> stringCellValue
            CellType.BOOLEAN -> booleanCellValue
            CellType.BLANK, null -> null
            else -> throw IllegalStateException(cellType.name)
        }
    }
}