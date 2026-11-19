package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Robi WiFi", appName)
  }

  @Test
  fun `qr bitmap is generated for YouTube channel`() {
    val bmp = com.example.util.QrCodeGenerator.generateQrBitmap("https://www.youtube.com/@robiwifibd", 128)
    org.junit.Assert.assertNotNull(bmp)
    assertEquals(128, bmp?.width)
    assertEquals(128, bmp?.height)
  }
}
