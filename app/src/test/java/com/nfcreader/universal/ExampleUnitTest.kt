package com.nfcreader.universal

import com.nfcreader.universal.data.DemoCards
import com.nfcreader.universal.data.StationDatabase
import com.nfcreader.universal.model.TagStandard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testStationDatabaseResolution() {
        val tokyo = StationDatabase.resolveStation(0x01, 0x01)
        assertEquals("東京", tokyo.stationName)
        assertEquals("JR東日本", tokyo.companyName)

        val shinjuku = StationDatabase.resolveStation(0x02, 0x0A)
        assertEquals("新宿", shinjuku.stationName)
        assertEquals("JR東日本", shinjuku.companyName)

        val shibuya = StationDatabase.resolveStation(0x01, 0x0B)
        assertEquals("渋谷", shibuya.stationName)

        val osaka = StationDatabase.resolveStation(0x70, 0x01)
        assertEquals("大阪", osaka.stationName)
        assertEquals("JR西日本", osaka.companyName)
    }

    @Test
    fun testSuicaDemoParsing() {
        val suica = DemoCards.createSuicaDemo()
        assertEquals(TagStandard.FELICA_TRANSIT, suica.standard)
        assertNotNull(suica.transitCardInfo)

        val info = suica.transitCardInfo!!
        assertEquals(1540, info.currentBalance)
        assertEquals(5, info.transactions.size)

        val firstTx = info.transactions[0]
        assertEquals("自動改札機", firstTx.terminalType)
        assertEquals("運賃支払 (乗車)", firstTx.processType)
        assertEquals("渋谷", firstTx.entryStationName)
        assertEquals("横浜", firstTx.exitStationName)
        assertEquals(1540, firstTx.balance)
    }
}
