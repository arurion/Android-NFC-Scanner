package com.nfcreader.universal.data

import android.content.Context
import com.nfcreader.universal.model.StationInfo
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Built-in Cyberne (サイバネ) Railway Station and Line Resolver.
 * Resolves Line Codes and Station Codes stored in FeliCa transit history blocks.
 * Supports nearly 6,000 railway stations across Japan loaded from ekicode.csv asset.
 */
object StationDatabase {

    private val assetLoadedMap = ConcurrentHashMap<Triple<Int, Int, Int>, StationInfo>()
    private val assetLineStationMap = ConcurrentHashMap<Pair<Int, Int>, StationInfo>()
    private val allStationList = mutableListOf<StationInfo>()
    @Volatile private var isInitialized = false

    private val lineMap = mapOf(
        0x01 to "JR東日本 山手線/東海道本線",
        0x02 to "JR東日本 中央本線",
        0x03 to "JR東日本 東海道本線・横須賀線",
        0x04 to "JR東日本 総武本線",
        0x05 to "JR東日本 京浜東北・根岸線",
        0x06 to "JR東日本 常磐線",
        0x07 to "JR東日本 埼京・川越線",
        0x08 to "JR東日本 武蔵野線",
        0x09 to "JR東日本 南武線",
        0x0A to "JR東日本 横浜線",
        0x10 to "JR東日本 高崎線",
        0x11 to "JR東日本 宇都宮線",
        0x20 to "東京メトロ 銀座線",
        0x21 to "東京メトロ 丸ノ内線",
        0x22 to "東京メトロ 日比谷線",
        0x23 to "東京メトロ 東西線",
        0x24 to "東京メトロ 千代田線",
        0x25 to "東京メトロ 有楽町線",
        0x26 to "東京メトロ 半蔵門線",
        0x27 to "東京メトロ 南北線",
        0x28 to "東京メトロ 副都心線",
        0x30 to "都営 大江戸線",
        0x31 to "都営 浅草線",
        0x32 to "都営 三田線",
        0x33 to "都営 新宿線",
        0x40 to "東急 東横線",
        0x41 to "東急 目黒線",
        0x42 to "東急 田園都市線",
        0x43 to "東急 大井町線",
        0x44 to "東急 池上線",
        0x50 to "小田急 小田原線",
        0x51 to "小田急 江ノ島線",
        0x52 to "小田急 多摩線",
        0x60 to "京王 京王線",
        0x61 to "京王 井の頭線",
        0x62 to "京王 相模原線",
        0x65 to "東武 スカイツリーライン",
        0x66 to "東武 東上線",
        0x68 to "西武 新宿線",
        0x69 to "西武 池袋線",
        0x6C to "京急 本線",
        0x6E to "京成 本線",
        0x70 to "JR西日本 大阪環状線",
        0x71 to "JR西日本 JR京都・神戸線",
        0x72 to "JR西日本 JR宝塚線・東西線",
        0x73 to "JR西日本 学研都市線",
        0x74 to "JR西日本 大和路線",
        0x75 to "JR西日本 阪和線",
        0x80 to "Osaka Metro 御堂筋線",
        0x81 to "Osaka Metro 谷町線",
        0x82 to "Osaka Metro 四つ橋線",
        0x83 to "Osaka Metro 中央線",
        0x84 to "Osaka Metro 千日前線",
        0x85 to "Osaka Metro 堺筋線",
        0x86 to "Osaka Metro 長堀鶴見緑地線",
        0x88 to "阪急 神戸本線",
        0x89 to "阪急 京都本線",
        0x8A to "阪神 本線",
        0x90 to "名鉄 名古屋本線",
        0x91 to "近鉄 名古屋線",
        0x92 to "近鉄 大阪線・奈良線"
    )

    private val stationMap = mapOf(
        (0x01 to 0x01) to ("JR東日本" to "東京"),
        (0x01 to 0x02) to ("JR東日本" to "有楽町"),
        (0x01 to 0x03) to ("JR東日本" to "新橋"),
        (0x01 to 0x04) to ("JR東日本" to "浜松町"),
        (0x01 to 0x05) to ("JR東日本" to "田町"),
        (0x01 to 0x06) to ("JR東日本" to "品川"),
        (0x01 to 0x07) to ("JR東日本" to "大崎"),
        (0x01 to 0x08) to ("JR東日本" to "五反田"),
        (0x01 to 0x09) to ("JR東日本" to "目黒"),
        (0x01 to 0x0A) to ("JR東日本" to "恵比寿"),
        (0x01 to 0x0B) to ("JR東日本" to "渋谷"),
        (0x01 to 0x0C) to ("JR東日本" to "原宿"),
        (0x01 to 0x0D) to ("JR東日本" to "代々木"),
        (0x01 to 0x0E) to ("JR東日本" to "新宿"),
        (0x01 to 0x0F) to ("JR東日本" to "新大久保"),
        (0x01 to 0x10) to ("JR東日本" to "高田馬場"),
        (0x01 to 0x11) to ("JR東日本" to "目白"),
        (0x01 to 0x12) to ("JR東日本" to "池袋"),
        (0x01 to 0x13) to ("JR東日本" to "大塚"),
        (0x01 to 0x14) to ("JR東日本" to "巣鴨"),
        (0x01 to 0x15) to ("JR東日本" to "駒込"),
        (0x01 to 0x16) to ("JR東日本" to "田端"),
        (0x01 to 0x17) to ("JR東日本" to "西日暮里"),
        (0x01 to 0x18) to ("JR東日本" to "日暮里"),
        (0x01 to 0x19) to ("JR東日本" to "鶯谷"),
        (0x01 to 0x1A) to ("JR東日本" to "上野"),
        (0x01 to 0x1B) to ("JR東日本" to "御徒町"),
        (0x01 to 0x1C) to ("JR東日本" to "秋葉原"),
        (0x01 to 0x1D) to ("JR東日本" to "神田"),
        (0x01 to 0x1E) to ("JR東日本" to "高輪ゲートウェイ"),

        // Chuo Line
        (0x02 to 0x01) to ("JR東日本" to "神田"),
        (0x02 to 0x02) to ("JR東日本" to "御茶ノ水"),
        (0x02 to 0x03) to ("JR東日本" to "水道橋"),
        (0x02 to 0x04) to ("JR東日本" to "飯田橋"),
        (0x02 to 0x05) to ("JR東日本" to "市ケ谷"),
        (0x02 to 0x06) to ("JR東日本" to "四ツ谷"),
        (0x02 to 0x07) to ("JR東日本" to "信濃町"),
        (0x02 to 0x08) to ("JR東日本" to "千駄ケ谷"),
        (0x02 to 0x09) to ("JR東日本" to "代々木"),
        (0x02 to 0x0A) to ("JR東日本" to "新宿"),
        (0x02 to 0x0B) to ("JR東日本" to "大久保"),
        (0x02 to 0x0C) to ("JR東日本" to "東中野"),
        (0x02 to 0x0D) to ("JR東日本" to "中野"),
        (0x02 to 0x0E) to ("JR東日本" to "高円寺"),
        (0x02 to 0x0F) to ("JR東日本" to "阿佐ケ谷"),
        (0x02 to 0x10) to ("JR東日本" to "荻窪"),
        (0x02 to 0x11) to ("JR東日本" to "西荻窪"),
        (0x02 to 0x12) to ("JR東日本" to "吉祥寺"),
        (0x02 to 0x13) to ("JR東日本" to "三鷹"),
        (0x02 to 0x14) to ("JR東日本" to "武蔵境"),
        (0x02 to 0x15) to ("JR東日本" to "東小金井"),
        (0x02 to 0x16) to ("JR東日本" to "武蔵小金井"),
        (0x02 to 0x17) to ("JR東日本" to "国分寺"),
        (0x02 to 0x18) to ("JR東日本" to "西国分寺"),
        (0x02 to 0x19) to ("JR東日本" to "国立"),
        (0x02 to 0x1A) to ("JR東日本" to "立川"),
        (0x02 to 0x1B) to ("JR東日本" to "日野"),
        (0x02 to 0x1C) to ("JR東日本" to "豊田"),
        (0x02 to 0x1D) to ("JR東日本" to "八王子"),

        // Tokaido/Yokohama
        (0x03 to 0x01) to ("JR東日本" to "東京"),
        (0x03 to 0x02) to ("JR東日本" to "新橋"),
        (0x03 to 0x03) to ("JR東日本" to "品川"),
        (0x03 to 0x04) to ("JR東日本" to "川崎"),
        (0x03 to 0x05) to ("JR東日本" to "横浜"),
        (0x03 to 0x06) to ("JR東日本" to "戸塚"),
        (0x03 to 0x07) to ("JR東日本" to "大船"),
        (0x03 to 0x08) to ("JR東日本" to "藤沢"),
        (0x03 to 0x09) to ("JR東日本" to "茅ケ崎"),
        (0x03 to 0x0A) to ("JR東日本" to "平塚"),
        (0x03 to 0x10) to ("JR東日本" to "小田原"),

        // Sobu
        (0x04 to 0x01) to ("JR東日本" to "秋葉原"),
        (0x04 to 0x02) to ("JR東日本" to "浅草橋"),
        (0x04 to 0x03) to ("JR東日本" to "両国"),
        (0x04 to 0x04) to ("JR東日本" to "錦糸町"),
        (0x04 to 0x05) to ("JR東日本" to "亀戸"),
        (0x04 to 0x07) to ("JR東日本" to "新小岩"),
        (0x04 to 0x09) to ("JR東日本" to "市川"),
        (0x04 to 0x0C) to ("JR東日本" to "西船橋"),
        (0x04 to 0x0D) to ("JR東日本" to "船橋"),
        (0x04 to 0x0F) to ("JR東日本" to "津田沼"),
        (0x04 to 0x15) to ("JR東日本" to "千葉"),

        // Keihin Tohoku
        (0x05 to 0x01) to ("JR東日本" to "大宮"),
        (0x05 to 0x02) to ("JR東日本" to "さいたま新都心"),
        (0x05 to 0x05) to ("JR東日本" to "浦和"),
        (0x05 to 0x06) to ("JR東日本" to "南浦和"),
        (0x05 to 0x09) to ("JR東日本" to "川口"),
        (0x05 to 0x0A) to ("JR東日本" to "赤羽"),
        (0x05 to 0x10) to ("JR東日本" to "蒲田"),
        (0x05 to 0x11) to ("JR東日本" to "川崎"),
        (0x05 to 0x12) to ("JR東日本" to "鶴見"),
        (0x05 to 0x15) to ("JR東日本" to "横浜"),
        (0x05 to 0x16) to ("JR東日本" to "桜木町"),
        (0x05 to 0x17) to ("JR東日本" to "関内"),

        // Metro Ginza
        (0x20 to 0x01) to ("東京メトロ" to "浅草"),
        (0x20 to 0x04) to ("東京メトロ" to "上野"),
        (0x20 to 0x07) to ("東京メトロ" to "神田"),
        (0x20 to 0x08) to ("東京メトロ" to "三越前"),
        (0x20 to 0x09) to ("東京メトロ" to "日本橋"),
        (0x20 to 0x0B) to ("東京メトロ" to "銀座"),
        (0x20 to 0x0C) to ("東京メトロ" to "新橋"),
        (0x20 to 0x0D) to ("東京メトロ" to "虎ノ門"),
        (0x20 to 0x0F) to ("東京メトロ" to "赤坂見附"),
        (0x20 to 0x10) to ("東京メトロ" to "青山一丁目"),
        (0x20 to 0x12) to ("東京メトロ" to "表参道"),
        (0x20 to 0x13) to ("東京メトロ" to "渋谷"),

        // JR West Osaka Loop
        (0x70 to 0x01) to ("JR西日本" to "大阪"),
        (0x70 to 0x02) to ("JR西日本" to "福島"),
        (0x70 to 0x04) to ("JR西日本" to "西九条"),
        (0x70 to 0x09) to ("JR西日本" to "新今宮"),
        (0x70 to 0x0A) to ("JR西日本" to "天王寺"),
        (0x70 to 0x0D) to ("JR西日本" to "鶴橋"),
        (0x70 to 0x13) to ("JR西日本" to "京橋")
    )

    fun initializeFromAssets(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                context.assets.open("ekicode.csv").use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        // Skip header: 地域コード,路線コード,駅順コード,会社名,線区名,駅名
                        reader.readLine()
                        var line: String? = reader.readLine()
                        while (line != null) {
                            val parts = line.split(",")
                            if (parts.size >= 6) {
                                val areaStr = parts[0].trim()
                                val lineHex = parts[1].trim()
                                val stationHex = parts[2].trim()
                                val company = parts[3].trim()
                                val lineName = parts[4].trim()
                                val stationName = parts[5].trim()

                                val areaCode = areaStr.toIntOrNull() ?: 0
                                val lineCode = lineHex.toIntOrNull(16) ?: 0
                                val stationCode = stationHex.toIntOrNull(16) ?: 0

                                val info = StationInfo(
                                    lineCode = lineCode,
                                    stationCode = stationCode,
                                    lineName = "$company $lineName",
                                    stationName = stationName,
                                    companyName = company,
                                    areaCode = areaCode
                                )

                                assetLoadedMap[Triple(areaCode, lineCode, stationCode)] = info
                                assetLineStationMap[Pair(lineCode, stationCode)] = info
                                allStationList.add(info)
                            }
                            line = reader.readLine()
                        }
                    }
                }
                isInitialized = true
            } catch (_: Exception) {}
        }
    }

    fun resolveStation(lineCode: Int, stationCode: Int): StationInfo {
        val cleanLineCode = lineCode and 0x7F
        val area = (lineCode ushr 6) and 0x03

        // 1. Check loaded asset map with exact area
        val fromAsset = assetLoadedMap[Triple(area, cleanLineCode, stationCode)]
        if (fromAsset != null) return fromAsset

        // 2. Check hardcoded top dictionary
        val foundStation = stationMap[cleanLineCode to stationCode]
        val lineName = lineMap[cleanLineCode] ?: "線区コード [0x%02X] (エリア%d)".format(cleanLineCode, area)

        if (foundStation != null) {
            return StationInfo(
                lineCode = cleanLineCode,
                stationCode = stationCode,
                lineName = lineName,
                stationName = foundStation.second,
                companyName = foundStation.first,
                areaCode = area
            )
        }

        // 3. Check loaded asset map without area restriction
        val fromAssetLineOnly = assetLineStationMap[Pair(cleanLineCode, stationCode)]
        if (fromAssetLineOnly != null) return fromAssetLineOnly

        // 4. Default fallback with clear technical codes
        val areaLabel = when (area) {
            0 -> "関東エリア (JR各社)"
            1 -> "関西・中部エリア"
            2 -> "関東私鉄・公営エリア"
            3 -> "その他 (九州・北海道等)"
            else -> "地域: $area"
        }

        return StationInfo(
            lineCode = cleanLineCode,
            stationCode = stationCode,
            lineName = lineName,
            stationName = "駅コード [0x%02X]".format(stationCode),
            companyName = areaLabel,
            areaCode = area
        )
    }

    fun searchStations(query: String, limit: Int = 40): List<StationInfo> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return allStationList.take(limit)

        return allStationList.filter {
            it.stationName.lowercase().contains(q) ||
                    it.lineName.lowercase().contains(q) ||
                    it.companyName.lowercase().contains(q) ||
                    "0x%02X".format(it.stationCode).lowercase().contains(q) ||
                    "0x%02X".format(it.lineCode).lowercase().contains(q)
        }.take(limit)
    }

    fun getLoadedStationCount(): Int = if (allStationList.isNotEmpty()) allStationList.size else stationMap.size
}
