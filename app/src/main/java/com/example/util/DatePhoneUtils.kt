package com.example.util

import com.example.data.model.SrReport
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DatePhoneUtils {
    private val dhakaTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun formatPhoneNumber(num: String?): String {
        var cleaned = (num ?: "").trim().replace(Regex("\\D"), "")
        if (cleaned.startsWith("880") && cleaned.length == 13) {
            cleaned = "0" + cleaned.substring(3)
        } else if (cleaned.length == 10 && !cleaned.startsWith("0")) {
            cleaned = "0$cleaned"
        }
        return cleaned
    }

    fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = dhakaTimeZone
        }
        return sdf.format(Date())
    }

    fun getFormattedInvoiceDate(): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).apply {
            timeZone = dhakaTimeZone
        }
        return sdf.format(Date())
    }

    fun getFormattedNowDateTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).apply {
            timeZone = dhakaTimeZone
        }
        return sdf.format(Date())
    }

    fun formatAmountPlain(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    fun formatAmountComma(value: Double): String {
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return nf.format(value)
    }

    fun formatUnitDisplay(value: Double): String {
        if (value == 0.0) return "0"
        val nf = NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 0
        }
        return nf.format(value)
    }

    fun isReportForToday(report: SrReport, todayKey: String): Boolean {
        if (report.dateKey.isNotBlank()) {
            return report.dateKey.trim() == todayKey
        }
        if (report.dateTime.isNotBlank()) {
            val candidateFormats = listOf(
                "dd MMM yyyy, hh:mm a",
                "dd MMM yyyy, HH:mm",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
            )
            for (pattern in candidateFormats) {
                try {
                    val parser = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                        timeZone = dhakaTimeZone
                    }
                    val parsed = parser.parse(report.dateTime.trim())
                    if (parsed != null) {
                        val out = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                            timeZone = dhakaTimeZone
                        }
                        return out.format(parsed) == todayKey
                    }
                } catch (_: Exception) {
                }
            }
        }
        return false
    }
}
