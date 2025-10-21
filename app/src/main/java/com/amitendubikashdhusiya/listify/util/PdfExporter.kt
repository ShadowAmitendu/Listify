package com.amitendubikashdhusiya.listify.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
        private const val PAGE_WIDTH = 595 // A4 width
        private const val PAGE_HEIGHT = 842 // A4 height
        private const val MARGIN = 40
        private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)
        private const val LINE_HEIGHT = 24
        private const val SECTION_SPACING = 16
    }

    fun exportToPdf(items: List<ShoppingItem>): Uri? {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var yPosition = drawHeader(canvas)

        // Group items by category
        val groupedItems = items.groupBy { it.category }
        val sortedCategories = groupedItems.keys.sorted()

        sortedCategories.forEach { category ->
            val categoryItems = groupedItems[category] ?: emptyList()

            // Check if we need a new page for category section
            val estimatedHeight = 60 + (categoryItems.size * LINE_HEIGHT)
            if (yPosition + estimatedHeight > PAGE_HEIGHT - MARGIN - 150) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo =
                    PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = MARGIN + 20
            }

            yPosition += SECTION_SPACING
            yPosition = drawCategorySection(canvas, category, categoryItems, yPosition)
        }

        // Check if footer fits
        if (yPosition > PAGE_HEIGHT - MARGIN - 180) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            yPosition = MARGIN + 40
        }

        yPosition += SECTION_SPACING * 2
        drawFooter(canvas, items, yPosition, pageNumber)
        pdfDocument.finishPage(page)

        return savePdf(pdfDocument)
    }

    private fun drawHeader(canvas: Canvas): Int {
        var yPos = MARGIN

        // Title
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.05f
        }
        canvas.drawText("SHOPPING LIST", MARGIN.toFloat(), yPos.toFloat(), titlePaint)
        yPos += 8

        // Underline
        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            yPos.toFloat(),
            (MARGIN + 200).toFloat(),
            yPos.toFloat(),
            linePaint
        )
        yPos += 30

        // Date and time info
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentDate = Date()

        val infoPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }

        canvas.drawText(
            "Generated: ${dateFormat.format(currentDate)}",
            MARGIN.toFloat(),
            yPos.toFloat(),
            infoPaint
        )
        yPos += 18
        canvas.drawText(
            "Time: ${timeFormat.format(currentDate)}",
            MARGIN.toFloat(),
            yPos.toFloat(),
            infoPaint
        )
        yPos += 30

        // Separator line
        val separatorPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            yPos.toFloat(),
            (PAGE_WIDTH - MARGIN).toFloat(),
            yPos.toFloat(),
            separatorPaint
        )

        return yPos + 25
    }

    private fun drawCategorySection(
        canvas: Canvas,
        category: String,
        items: List<ShoppingItem>,
        yPos: Int
    ): Int {
        var currentY = yPos

        // Category header box
        val boxPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRect(
            MARGIN.toFloat(),
            (currentY - 18).toFloat(),
            (PAGE_WIDTH - MARGIN).toFloat(),
            (currentY + 10).toFloat(),
            boxPaint
        )

        // Category name
        val categoryPaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            letterSpacing = 0.08f
        }
        canvas.drawText(
            category.uppercase(),
            (MARGIN + 10).toFloat(),
            currentY.toFloat(),
            categoryPaint
        )

        // Item count
        val countPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(
            "(${items.size} ${if (items.size == 1) "item" else "items"})",
            (PAGE_WIDTH - MARGIN - 10).toFloat(),
            currentY.toFloat(),
            countPaint
        )

        currentY += 30

        // Column headers
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("ITEM", (MARGIN + 35).toFloat(), currentY.toFloat(), headerPaint)

        headerPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "QTY",
            (PAGE_WIDTH - MARGIN - 80).toFloat(),
            currentY.toFloat(),
            headerPaint
        )

        headerPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "STATUS",
            (PAGE_WIDTH - MARGIN - 20).toFloat(),
            currentY.toFloat(),
            headerPaint
        )

        currentY += 8

        // Header underline
        val thinLinePaint = Paint().apply {
            color = Color.DKGRAY
            strokeWidth = 0.5f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            currentY.toFloat(),
            (PAGE_WIDTH - MARGIN).toFloat(),
            currentY.toFloat(),
            thinLinePaint
        )

        currentY += 18

        // Draw items
        items.forEachIndexed { index, item ->
            currentY = drawItemRow(canvas, item, currentY, index)
        }

        return currentY
    }

    private fun drawItemRow(canvas: Canvas, item: ShoppingItem, yPos: Int, index: Int): Int {
        var currentY = yPos

        // Row number
        val numberPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(
            "${index + 1}.",
            (MARGIN + 20).toFloat(),
            currentY.toFloat(),
            numberPaint
        )

        // Item name
        val itemPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
            if (item.isBought) {
                alpha = 150
            }
        }

        val maxNameWidth = CONTENT_WIDTH - 200f
        val itemText = truncateText(item.name, maxNameWidth, itemPaint)

        canvas.drawText(
            itemText,
            (MARGIN + 35).toFloat(),
            currentY.toFloat(),
            itemPaint
        )

        // Strikethrough if bought
        if (item.isBought) {
            val strikeWidth = itemPaint.measureText(itemText)
            val strikePaint = Paint().apply {
                color = Color.DKGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(
                (MARGIN + 35).toFloat(),
                (currentY - 4f),
                (MARGIN + 35 + strikeWidth),
                (currentY - 4f),
                strikePaint
            )
        }

        // Quantity
        val quantityPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val quantityText = "${item.quantity} ${item.unit}"
        canvas.drawText(
            quantityText,
            (PAGE_WIDTH - MARGIN - 80).toFloat(),
            currentY.toFloat(),
            quantityPaint
        )

        // Status checkbox
        val checkSize = 12f
        val checkX = PAGE_WIDTH - MARGIN - 26f
        val checkY = currentY - 9f

        val checkboxPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }

        canvas.drawRect(
            checkX,
            checkY,
            checkX + checkSize,
            checkY + checkSize,
            checkboxPaint
        )

        // Draw X if bought
        if (item.isBought) {
            val xPaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                checkX + 2,
                checkY + 2,
                checkX + checkSize - 2,
                checkY + checkSize - 2,
                xPaint
            )
            canvas.drawLine(
                checkX + checkSize - 2,
                checkY + 2,
                checkX + 2,
                checkY + checkSize - 2,
                xPaint
            )
        }

        currentY += LINE_HEIGHT

        // Light separator line
        val separatorPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 0.3f
        }
        canvas.drawLine(
            (MARGIN + 30).toFloat(),
            currentY.toFloat(),
            (PAGE_WIDTH - MARGIN - 10).toFloat(),
            currentY.toFloat(),
            separatorPaint
        )

        return currentY + 4
    }

    private fun drawFooter(canvas: Canvas, items: List<ShoppingItem>, yPos: Int, pageNum: Int) {
        var currentY = yPos

        // Thick separator
        val thickLinePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
        }
        canvas.drawLine(
            MARGIN.toFloat(),
            currentY.toFloat(),
            (PAGE_WIDTH - MARGIN).toFloat(),
            currentY.toFloat(),
            thickLinePaint
        )

        currentY += 25

        // Summary section
        val summaryPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("SUMMARY", MARGIN.toFloat(), currentY.toFloat(), summaryPaint)
        currentY += 22

        val totalItems = items.size
        val completedItems = items.count { it.isBought }
        val remainingItems = totalItems - completedItems
        val completionPercent = if (totalItems > 0) (completedItems * 100) / totalItems else 0

        val labelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Stats
        canvas.drawText("Total Items:", (MARGIN + 20).toFloat(), currentY.toFloat(), labelPaint)
        canvas.drawText(
            totalItems.toString(),
            (MARGIN + 250).toFloat(),
            currentY.toFloat(),
            valuePaint
        )
        currentY += 20

        canvas.drawText("Completed:", (MARGIN + 20).toFloat(), currentY.toFloat(), labelPaint)
        canvas.drawText(
            "$completedItems ($completionPercent%)",
            (MARGIN + 250).toFloat(),
            currentY.toFloat(),
            valuePaint
        )
        currentY += 20

        canvas.drawText("Remaining:", (MARGIN + 20).toFloat(), currentY.toFloat(), labelPaint)
        canvas.drawText(
            remainingItems.toString(),
            (MARGIN + 250).toFloat(),
            currentY.toFloat(),
            valuePaint
        )
        currentY += 30

        // Footer text
        val footerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText(
            "Generated by Listify - Smart Shopping Made Simple",
            (PAGE_WIDTH / 2).toFloat(),
            currentY.toFloat(),
            footerPaint
        )

        currentY += 15
        canvas.drawText(
            "Page $pageNum",
            (PAGE_WIDTH / 2).toFloat(),
            currentY.toFloat(),
            footerPaint
        )
    }

    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text

        var truncated = text
        while (paint.measureText("$truncated...") > maxWidth && truncated.length > 1) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }

    private fun savePdf(pdfDocument: PdfDocument): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Listify_ShoppingList_$timestamp.pdf"
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

    fun sharePdf(uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Listify Shopping List")
            putExtra(Intent.EXTRA_TEXT, "Here's my shopping list from Listify!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Shopping List"))
    }
}