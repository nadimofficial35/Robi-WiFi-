package com.example

import com.example.data.model.RobiQuizBank
import com.example.data.model.SrReport
import com.example.util.DatePhoneUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `phone normalization handles 880 and 10 digit formats`() {
        assertEquals("01812345678", DatePhoneUtils.formatPhoneNumber("+8801812345678"))
        assertEquals("01812345678", DatePhoneUtils.formatPhoneNumber("8801812345678"))
        assertEquals("01812345678", DatePhoneUtils.formatPhoneNumber("1812345678"))
        assertEquals("01812345678", DatePhoneUtils.formatPhoneNumber("01812-345-678"))
    }

    @Test
    fun `invoice formatting matches plain and comma totals`() {
        assertEquals("2,499", DatePhoneUtils.formatUnitDisplay(2499.0))
        assertEquals("0", DatePhoneUtils.formatUnitDisplay(0.0))
        assertEquals("3499.00", DatePhoneUtils.formatAmountPlain(3499.0))
        assertEquals("3,499.00", DatePhoneUtils.formatAmountComma(3499.0))
    }

    @Test
    fun `report date matching verifies dateKey`() {
        val today = "2026-11-19"
        val r1 = SrReport(dateKey = "2026-11-19")
        val r2 = SrReport(dateKey = "2026-11-18")
        assertTrue(DatePhoneUtils.isReportForToday(r1, today))
        assertFalse(DatePhoneUtils.isReportForToday(r2, today))
    }

    @Test
    fun `quiz bank contains 5 questions with 4 options each`() {
        assertEquals(5, RobiQuizBank.questions.size)
        RobiQuizBank.questions.forEach { q ->
            assertEquals(4, q.options.size)
        }
    }
}
