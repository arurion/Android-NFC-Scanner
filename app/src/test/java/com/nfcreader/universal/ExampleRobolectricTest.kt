package com.nfcreader.universal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nfcreader.universal.data.DemoCards
import com.nfcreader.universal.data.TagConverter
import com.nfcreader.universal.model.TagStandard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("NFC Reader", appName)
    }

    @Test
    fun `test tag converter with json serialization in robolectric`() {
        val suica = DemoCards.createSuicaDemo()
        val entity = TagConverter.toEntity(suica)

        assertEquals("01:2E:4B:99:A1:02:88:F3", entity.uidHex)
        assertEquals("FELICA_TRANSIT", entity.standardName)
        assertEquals(1540, entity.balance)

        val restored = TagConverter.toData(entity)
        assertEquals(suica.idHex, restored.idHex)
        assertEquals(suica.standard, restored.standard)
        assertNotNull(restored.transitCardInfo)
        assertEquals(1540, restored.transitCardInfo?.currentBalance)
        assertEquals(5, restored.transitCardInfo?.transactions?.size)
    }
}
