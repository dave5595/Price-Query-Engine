package david.demo.data

import david.demo.common.SupportedSources.Companion.BARCLAYS
import david.demo.common.SupportedSources.Companion.CITI
import david.demo.common.SupportedSources.Companion.DBS
import david.demo.common.SupportedSources.Companion.REUTERS
import david.demo.common.SupportedSources.Companion.UOB
import david.demo.common.SupportedCurrencyPairs.Companion.EUR_USD
import david.demo.common.SupportedCurrencyPairs.Companion.GBP_USD
import david.demo.common.SupportedCurrencyPairs.Companion.AUD_USD
import david.demo.common.SupportedCurrencyPairs.Companion.USD_CAD
import david.demo.common.SupportedCurrencyPairs.Companion.USD_JPY
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class PriceQuoteGenerator(timestampDistributionOffset: Long = 1) {
    private val end: LocalDateTime = LocalDateTime.now()
    private val start: LocalDateTime = end.minusHours(timestampDistributionOffset)
    private val timestampRandomizer: RandomDateTimeGenerator = RandomDateTimeGenerator(start, end)
    private val bidAskPriceGenerator: BidAskPriceGenerator = BidAskPriceGenerator()
    private val dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
    private val random = Random()

    fun generate(amount: Int): Array<PriceQuote> {
        val quotes = Array(amount) { PriceQuote() }
        for (idx in quotes.indices) {
            val source = sources[random.nextInt(sources.size) % sources.size]
            val symbol = currPairs[random.nextInt(sources.size) % currPairs.size]
            val symbolPriceRange =
                currPriceRanges[symbol] ?: throw IllegalArgumentException("Symbol of $symbol is not supported")
            val minPrice = symbolPriceRange[0]
            val maxPrice = symbolPriceRange[1]
            val spread = symbolPriceRange[2]
            val bidAskPair = bidAskPriceGenerator.generate(minPrice, maxPrice, spread)
            val quote = quotes[idx]
            quote.source = source
            quote.symbol = symbol
            quote.bidPrice = bidAskPair.bid
            quote.askPrice = bidAskPair.ask
            quote.timestampMS = LocalDateTime.parse(timestampRandomizer.randomize(), dtf).toInstant(ZoneOffset.UTC).toEpochMilli()
        }
        return quotes
    }

    companion object {
        private val sources: Array<String> = arrayOf(CITI, DBS, REUTERS, UOB, BARCLAYS)
        private val currPairs: Array<String> = arrayOf(EUR_USD, GBP_USD, USD_JPY, AUD_USD, USD_CAD)
        private val currPriceRanges: Map<String, Array<Double>> = hashMapOf(
            EUR_USD to arrayOf(0.95, 1.6, 0.1),
            GBP_USD to arrayOf(1.0, 2.0, 0.01),
            USD_JPY to arrayOf(75.5, 160.8, 5.0),
            AUD_USD to arrayOf(0.48, 1.49, 0.1),
            USD_CAD to arrayOf(0.93, 1.61, 0.1)
        )
    }
}



