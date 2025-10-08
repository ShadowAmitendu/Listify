package com.amitendubikashdhusiya.listify.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.amitendubikashdhusiya.listify.data.entity.ShoppingItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfExporter(private val context: Context) {

    companion object {
        private const val PAGE_WIDTH = 280 // Very slim receipt width
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 20
        private const val LINE_HEIGHT = 22

        // Colors
        private const val PRIMARY_COLOR = 0xFF4CAF50.toInt() // Green
        private const val ACCENT_ORANGE = 0xFFFF9800.toInt()
        private const val TEXT_PRIMARY = 0xFF424242.toInt()
        private const val TEXT_LIGHT = 0xFF9E9E9E.toInt()
        private const val DIVIDER_COLOR = 0xFFE0E0E0.toInt()
    }

    fun exportToPdf(items: List<ShoppingItem>, useColor: Boolean = true): Uri? {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPosition = drawHeader(canvas, useColor)

        // Group items by category
        val groupedItems = items.groupBy { it.category }

        groupedItems.forEach { (category, categoryItems) ->
            // Check if we need a new page for category header
            if (yPosition > PAGE_HEIGHT - MARGIN - 100) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo =
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = MARGIN + 20
            }

            yPosition = drawDashedLine(canvas, yPosition, useColor)
            yPosition =
                drawCategoryHeader(canvas, category, yPosition, categoryItems.size, useColor)
            yPosition = drawDashedLine(canvas, yPosition, useColor)

            categoryItems.forEach { item ->
                // Check if we need a new page
                if (yPosition > PAGE_HEIGHT - MARGIN - 80) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo =
                        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN + 20
                }

                yPosition = drawItem(canvas, item, yPosition, useColor)
            }

            yPosition += 5
        }

        // Check if footer fits, otherwise create new page
        if (yPosition > PAGE_HEIGHT - MARGIN - 100) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = MARGIN + 20
        }

        drawFooter(canvas, items, yPosition, useColor)
        pdfDocument.finishPage(page)

        // Save the PDF
        return try {
            val colorMode = if (useColor) "color" else "mono"
            val fileName = "listify_${colorMode}_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun drawHeader(canvas: Canvas, useColor: Boolean): Int {
        var yPos = 0

        // Green header bar
        val headerPaint = Paint().apply {
            color = if (useColor) PRIMARY_COLOR else Color.DKGRAY
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 60f, headerPaint)

        // Logo and app name
        yPos = 22

        // Simple bag icon
        val bagPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val centerX = PAGE_WIDTH / 2f - 25f

        // Bag body
        val bagPath = Path().apply {
            addRoundRect(
                centerX, yPos - 8f,
                centerX + 16f, yPos + 10f,
                3f, 3f, Path.Direction.CW
            )
        }
        canvas.drawPath(bagPath, bagPaint)

        // Bag handle
        val handlePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val handlePath = Path().apply {
            moveTo(centerX + 4f, yPos - 8f)
            cubicTo(
                centerX + 4f, yPos - 12f,
                centerX + 12f, yPos - 12f,
                centerX + 12f, yPos - 8f
            )
        }
        canvas.drawPath(handlePath, handlePaint)

        // App name
        val appNamePaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("LISTIFY", centerX + 22f, yPos + 3f, appNamePaint)

        yPos += 15

        // Tagline
        val taglinePaint = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            textAlign = Paint.Align.CENTER
            alpha = 220
            isAntiAlias = true
        }
        canvas.drawText(
            "Smart Shopping List",
            PAGE_WIDTH / 2f,
            yPos.toFloat(),
            taglinePaint
        )

        yPos = 70

        // Date and time
        val dateFormat = SimpleDateFormat("MM/dd/yyyy    hh:mm a", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val datePaint = Paint().apply {
            color = if (useColor) TEXT_LIGHT else Color.GRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            currentDate,
            PAGE_WIDTH / 2f,
            yPos.toFloat(),
            datePaint
        )

        return yPos + 15
    }

    private fun drawDashedLine(canvas: Canvas, yPos: Int, useColor: Boolean): Int {
        val dashedPaint = Paint().apply {
            color = if (useColor) DIVIDER_COLOR else Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
        }

        canvas.drawLine(
            MARGIN.toFloat(),
            yPos.toFloat(),
            (PAGE_WIDTH - MARGIN).toFloat(),
            yPos.toFloat(),
            dashedPaint
        )

        return yPos + 12
    }

    private fun drawCategoryHeader(
        canvas: Canvas,
        category: String,
        yPos: Int,
        itemCount: Int,
        useColor: Boolean
    ): Int {
        var currentY = yPos

        // Category icon (colored square)
        val squarePaint = Paint().apply {
            color = if (useColor) PRIMARY_COLOR else Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(
            MARGIN.toFloat(),
            (currentY - 8).toFloat(),
            (MARGIN + 8).toFloat(),
            currentY.toFloat(),
            squarePaint
        )

        // Category name
        val categoryPaint = Paint().apply {
            color = if (useColor) TEXT_PRIMARY else Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(
            category.uppercase(),
            (MARGIN + 14).toFloat(),
            currentY.toFloat(),
            categoryPaint
        )

        // Item count
        val countPaint = Paint().apply {
            color = if (useColor) ACCENT_ORANGE else Color.DKGRAY
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(
            "$itemCount ${if (itemCount == 1) "item" else "items"}",
            (PAGE_WIDTH - MARGIN).toFloat(),
            currentY.toFloat(),
            countPaint
        )

        return currentY + 15
    }

    private fun drawItem(canvas: Canvas, item: ShoppingItem, yPos: Int, useColor: Boolean): Int {
        var currentY = yPos

        // Checkbox
        val checkSize = 9f
        val checkboxPaint = Paint().apply {
            color = if (item.isBought) {
                if (useColor) PRIMARY_COLOR else Color.BLACK
            } else {
                if (useColor) TEXT_LIGHT else Color.GRAY
            }
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        canvas.drawRect(
            MARGIN.toFloat(),
            (currentY - checkSize).toFloat(),
            (MARGIN + checkSize),
            currentY.toFloat(),
            checkboxPaint
        )

        // Checkmark if bought
        if (item.isBought) {
            val checkPaint = Paint().apply {
                color = if (useColor) PRIMARY_COLOR else Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
            }
            val checkPath = Path().apply {
                moveTo(MARGIN + 2f, currentY - 4.5f)
                lineTo(MARGIN + 4f, currentY - 2f)
                lineTo(MARGIN + 7f, currentY - 7f)
            }
            canvas.drawPath(checkPath, checkPaint)
        }

        // Item name
        val itemPaint = Paint().apply {
            color = if (item.isBought) {
                if (useColor) TEXT_LIGHT else Color.GRAY
            } else {
                if (useColor) TEXT_PRIMARY else Color.BLACK
            }
            textSize = 10f
            isAntiAlias = true
        }

        val maxWidth = PAGE_WIDTH - MARGIN * 2 - 60f
        val itemText = if (itemPaint.measureText(item.name) > maxWidth) {
            var truncated = item.name
            while (itemPaint.measureText(truncated + "...") > maxWidth && truncated.length > 1) {
                truncated = truncated.dropLast(1)
            }
            truncated + "..."
        } else {
            item.name
        }

        canvas.drawText(
            itemText,
            (MARGIN + 15).toFloat(),
            currentY.toFloat(),
            itemPaint
        )

        // Quantity
        val quantityText = "${item.quantity} ${item.unit}"
        val quantityPaint = Paint().apply {
            color = if (useColor) TEXT_PRIMARY else Color.BLACK
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(
            quantityText,
            (PAGE_WIDTH - MARGIN).toFloat(),
            currentY.toFloat(),
            quantityPaint
        )

        // Strikethrough if bought
        if (item.isBought) {
            val strikeWidth = itemPaint.measureText(itemText)
            val strikePaint = Paint().apply {
                color = if (useColor) TEXT_LIGHT else Color.GRAY
                strokeWidth = 1f
            }
            canvas.drawLine(
                (MARGIN + 15).toFloat(),
                (currentY - 3.5f),
                (MARGIN + 15 + strikeWidth),
                (currentY - 3.5f),
                strikePaint
            )
        }

        return currentY + LINE_HEIGHT
    }

    private fun drawFooter(
        canvas: Canvas,
        items: List<ShoppingItem>,
        yPos: Int,
        useColor: Boolean
    ) {
        var currentY = yPos

        drawDashedLine(canvas, currentY, useColor)
        currentY += 8

        val totalItems = items.size
        val boughtItems = items.count { it.isBought }
        val remainingItems = totalItems - boughtItems

        val labelPaint = Paint().apply {
            color = if (useColor) TEXT_PRIMARY else Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = if (useColor) TEXT_PRIMARY else Color.BLACK
            textSize = 10f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Total
        canvas.drawText("Total Items", (MARGIN + 5).toFloat(), currentY.toFloat(), labelPaint)
        canvas.drawText(
            totalItems.toString(),
            (PAGE_WIDTH - MARGIN - 5).toFloat(),
            currentY.toFloat(),
            valuePaint
        )
        currentY += LINE_HEIGHT - 4

        // Bought
        canvas.drawText("Completed", (MARGIN + 5).toFloat(), currentY.toFloat(), labelPaint)
        valuePaint.color = if (useColor) PRIMARY_COLOR else Color.DKGRAY
        canvas.drawText(
            boughtItems.toString(),
            (PAGE_WIDTH - MARGIN - 5).toFloat(),
            currentY.toFloat(),
            valuePaint
        )
        currentY += LINE_HEIGHT - 4

        // Remaining
        canvas.drawText("Remaining", (MARGIN + 5).toFloat(), currentY.toFloat(), labelPaint)
        valuePaint.color = if (useColor) ACCENT_ORANGE else Color.BLACK
        canvas.drawText(
            remainingItems.toString(),
            (PAGE_WIDTH - MARGIN - 5).toFloat(),
            currentY.toFloat(),
            valuePaint
        )
        currentY += 18

        drawDashedLine(canvas, currentY, useColor)
        currentY += 10

        // Footer message
        val footerPaint = Paint().apply {
            color = if (useColor) TEXT_LIGHT else Color.GRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "Happy Shopping!",
            PAGE_WIDTH / 2f,
            currentY.toFloat(),
            footerPaint
        )
        currentY += 12

        canvas.drawText(
            "Powered by Listify",
            PAGE_WIDTH / 2f,
            currentY.toFloat(),
            footerPaint
        )
    }

    fun sharePdf(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "My Listify Shopping List")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Shopping List"))
    }
}