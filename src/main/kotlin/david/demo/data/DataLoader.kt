package david.demo.data

import david.demo.common.Files
import david.demo.common.round3
import java.io.BufferedReader
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DataLoader {

    companion object {
        private val dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

        fun fromCsv(filepath: String): List<PriceQuote> {
            return try {
                Files.loadFrom(filepath, Companion::toPriceQuotes)
            } catch (e: IOException) {
                emptyList()
            }
        }

        private fun toPriceQuotes(reader: BufferedReader): List<PriceQuote> {
            val priceQuotes: MutableList<PriceQuote> = ArrayList()
            var line = ""
            // Skip the CSV header
            reader.readLine()
            while (reader.readLine()?.also { line = it; } != null) {
                val fields = line
                    .split(",")
                    .dropLastWhile (String::isEmpty)
                if (fields.size == 5) {
                    val quote = PriceQuote()
                    quote.source = fields[0]
                    quote.symbol = fields[1]
                    quote.timestampMS = LocalDateTime.parse(fields[2], dtf).toInstant(ZoneOffset.UTC).toEpochMilli()
                    quote.bidPrice = fields[3].toDouble().round3()
                    quote.askPrice = fields[4].toDouble().round3()
                    priceQuotes.add(quote)
                }
            }
            return priceQuotes
        }
    }
}