package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.InvoiceData
import java.io.File
import java.io.FileOutputStream

data class PdfGenerationResult(
    val shareUri: Uri,
    val fileName: String,
    val savedToDownloads: Boolean
)

object InvoicePdfGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateAndSaveInvoicePdf(
        context: Context,
        invoice: InvoiceData,
        qrBitmap: Bitmap?
    ): PdfGenerationResult {
        val safeInv = invoice.invNumber
            .ifBlank { "Invoice" }
            .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val fileName = "Robi_WiFi_Invoice_$safeInv.pdf"

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 38, 38)
            style = Paint.Style.FILL
        }
        val whiteBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val whiteRegularPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val darkBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(17, 24, 39)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val darkNormalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(17, 24, 39)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val greenBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(22, 163, 74)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val blueLinkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(37, 99, 235)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(55, 65, 81)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val lightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(209, 213, 219)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(243, 244, 246)
            style = Paint.Style.FILL
        }

        // Top Red Brand Header
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 80f, redPaint)

        whiteBoldPaint.textSize = 20f
        whiteBoldPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("ROBI WIFI", 38f, 36f, whiteBoldPaint)

        whiteRegularPaint.textSize = 11f
        whiteRegularPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Invoice & Billing Statement", 38f, 56f, whiteRegularPaint)

        whiteBoldPaint.textSize = 18f
        whiteBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("INVOICE", 557f, 38f, whiteBoldPaint)

        whiteRegularPaint.textSize = 11f
        whiteRegularPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("+8801899999999", 557f, 58f, whiteRegularPaint)

        // Status Badge
        greenBoldPaint.textSize = 11f
        greenBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("STATUS: COMPLETED", 557f, 105f, greenBoldPaint)

        // Billed To Title
        darkBoldPaint.textSize = 14f
        darkBoldPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Billed To", 38f, 110f, darkBoldPaint)
        canvas.drawLine(38f, 118f, 557f, 118f, lightLinePaint)

        // Metadata Grid
        darkBoldPaint.textSize = 10.5f
        darkNormalPaint.textSize = 10.5f

        canvas.drawText("Customer Name:", 38f, 140f, darkBoldPaint)
        canvas.drawText(invoice.custName.ifBlank { "-" }, 145f, 140f, darkNormalPaint)

        canvas.drawText("Invoice No:", 350f, 140f, darkBoldPaint)
        canvas.drawText(invoice.invNumber.ifBlank { "-" }, 435f, 140f, darkNormalPaint)

        canvas.drawText("Customer E-mail:", 38f, 162f, darkBoldPaint)
        canvas.drawText(invoice.custEmail.ifBlank { "-" }, 145f, 162f, darkNormalPaint)

        canvas.drawText("Invoice Date:", 350f, 162f, darkBoldPaint)
        canvas.drawText(invoice.invDate.ifBlank { DatePhoneUtils.getFormattedInvoiceDate() }, 435f, 162f, darkNormalPaint)

        canvas.drawText("Payment Mode: COD", 38f, 188f, darkBoldPaint)

        // Table Header
        val tableTop = 212f
        val tableLeft = 38f
        val tableRight = 557f
        val headerHeight = 26f
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerHeight, headerBgPaint)
        canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + headerHeight, borderPaint)

        darkBoldPaint.textSize = 10f
        darkBoldPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("S.L.", 48f, tableTop + 17f, darkBoldPaint)
        canvas.drawText("Description", 92f, tableTop + 17f, darkBoldPaint)
        darkBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Unit", 392f, tableTop + 17f, darkBoldPaint)
        darkBoldPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Qty", 442f, tableTop + 17f, darkBoldPaint)
        darkBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Amount", 545f, tableTop + 17f, darkBoldPaint)

        // Table Rows
        val rows = listOf(
            listOf("01", invoice.desc1.ifBlank { "-" }, invoice.unit1.ifBlank { "0" }, "1", invoice.amt1.ifBlank { "0.00" }),
            listOf("02", invoice.desc2.ifBlank { "-" }, invoice.unit2.ifBlank { "0" }, "1", invoice.amt2.ifBlank { "0.00" }),
            listOf("03", invoice.desc3.ifBlank { "-" }, invoice.unit3.ifBlank { "0" }, "1", invoice.amt3.ifBlank { "0.00" })
        )

        var currentY = tableTop + headerHeight
        val rowHeight = 32f
        darkNormalPaint.textSize = 10f

        rows.forEach { row ->
            canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, borderPaint)

            darkNormalPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(row[0], 48f, currentY + 20f, darkNormalPaint)

            val desc = if (row[1].length > 46) row[1].take(46) + "..." else row[1]
            canvas.drawText(desc, 92f, currentY + 20f, darkNormalPaint)

            darkNormalPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(row[2], 392f, currentY + 20f, darkNormalPaint)

            darkNormalPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(row[3], 442f, currentY + 20f, darkNormalPaint)

            darkNormalPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(row[4], 545f, currentY + 20f, darkNormalPaint)

            currentY += rowHeight
        }

        // Totals + QR Code
        val totY = currentY + 32f
        if (qrBitmap != null) {
            val qrRect = RectF(38f, totY - 8f, 114f, totY + 68f)
            canvas.drawBitmap(qrBitmap, null, qrRect, null)
            canvas.drawRect(qrRect, lightLinePaint)
        }

        darkBoldPaint.textSize = 10.5f
        darkBoldPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Subtotal:", 365f, totY + 6f, darkBoldPaint)
        darkBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(invoice.subtotal.ifBlank { "0.00" }, 557f, totY + 6f, darkBoldPaint)

        darkBoldPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Discount (%):", 365f, totY + 26f, darkBoldPaint)
        darkBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("0.00", 557f, totY + 26f, darkBoldPaint)

        canvas.drawLine(360f, totY + 38f, 557f, totY + 38f, borderPaint)

        darkBoldPaint.textSize = 13f
        darkBoldPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("Total (BDT):", 365f, totY + 58f, darkBoldPaint)
        darkBoldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(invoice.total.ifBlank { "0.00" }, 557f, totY + 58f, darkBoldPaint)

        // Footer Links
        blueLinkPaint.textSize = 10f
        blueLinkPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("YouTube: https://www.youtube.com/@robiwifibd", 38f, totY + 102f, blueLinkPaint)
        canvas.drawText("Website: https://robiwifi.robi.com.bd", 38f, totY + 118f, blueLinkPaint)

        greenBoldPaint.textSize = 12f
        greenBoldPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank You for choosing Robi WiFi!", PAGE_WIDTH / 2f, totY + 158f, greenBoldPaint)

        document.finishPage(page)

        // Save to cache for FileProvider sharing/viewing
        val cacheInvoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val cacheFile = File(cacheInvoicesDir, fileName)
        FileOutputStream(cacheFile).use { fos ->
            document.writeTo(fos)
        }

        // Also save to MediaStore Downloads on Android 10+ (zero permissions required)
        var savedToDownloads = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        cacheFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    savedToDownloads = true
                }
            } catch (_: Exception) {
                savedToDownloads = false
            }
        }

        document.close()

        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
        return PdfGenerationResult(
            shareUri = shareUri,
            fileName = fileName,
            savedToDownloads = savedToDownloads
        )
    }
}
