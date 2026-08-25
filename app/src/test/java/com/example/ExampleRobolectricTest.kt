package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.qr.QrCodeGenerator
import com.example.qr.QrPayloadParser
import com.example.qr.QrStyleConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("QR Studio", appName)
    }

    @Test
    fun `parse url payload correctly`() {
        val parsed = QrPayloadParser.parse("https://github.com")
        assertEquals("URL", parsed.contentType)
        assertEquals("https://github.com", parsed.actionUrl)
    }

    @Test
    fun `parse wifi payload correctly`() {
        val wifiPayload = "WIFI:S:MyHomeWifi;T:WPA;P:Secret123;;"
        val parsed = QrPayloadParser.parse(wifiPayload)
        assertEquals("WIFI", parsed.contentType)
        assertEquals("MyHomeWifi", parsed.wifiSsid)
        assertEquals("Secret123", parsed.wifiPassword)
    }

    @Test
    fun `generate qr bitmap with custom style`() {
        val config = QrStyleConfig()
        val bitmap = QrCodeGenerator.generateQrBitmap(
            content = "https://example.com",
            size = 256,
            config = config,
            context = null
        )
        assertNotNull(bitmap)
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
    }
}
