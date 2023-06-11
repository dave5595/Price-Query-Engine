package david.demo.data

import david.demo.common.SupportedCurrencyPairs.Companion.AUD_USD
import david.demo.common.SupportedCurrencyPairs.Companion.EUR_USD
import david.demo.common.SupportedCurrencyPairs.Companion.GBP_USD
import david.demo.common.SupportedCurrencyPairs.Companion.USD_CAD
import david.demo.common.SupportedCurrencyPairs.Companion.USD_JPY
import david.demo.common.SupportedSources
import david.demo.common.SupportedSources.Companion.BARCLAYS
import david.demo.common.SupportedSources.Companion.CITI
import david.demo.common.SupportedSources.Companion.DBS
import david.demo.common.SupportedSources.Companion.REUTERS
import david.demo.common.SupportedSources.Companion.UOB
import david.demo.core.SidedPrice
import david.demo.core.Side

class PriceQuote {
    var source: String = ""
    var symbol: String = ""
    var askPrice: Double = Double.MIN_VALUE
    var bidPrice: Double = Double.MIN_VALUE
    var timestampMS: Long = Long.MIN_VALUE


    fun toSidedPriceList(): List<SidedPrice> {
        val priceList: MutableList<SidedPrice> = ArrayList()
        priceList.add(SidedPrice(source, symbol, Side.Bid, bidPrice, timestampMS))
        priceList.add(SidedPrice(source, symbol, Side.Ask, askPrice, timestampMS))
        return priceList
    }

    override fun toString(): String {
        return "PriceQuote(source='$source', symbol='$symbol', askPrice=$askPrice, bidPrice=$bidPrice, timestampMS=$timestampMS)"
    }

    companion object {

        fun byCiti(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(CITI, symbol, bidPrice, askPrice, timestampMS)
        }

        fun byDbs(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(DBS, symbol, bidPrice, askPrice, timestampMS)
        }

        fun byReuters(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(REUTERS, symbol, bidPrice, askPrice, timestampMS)
        }

        fun byUob(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(UOB, symbol, bidPrice, askPrice, timestampMS)
        }

        fun byBarclays(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(BARCLAYS, symbol, bidPrice, askPrice, timestampMS)
        }

        fun eur_usd(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, EUR_USD, bidPrice, askPrice, timestampMS)
        }

        fun usd_jpy(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, USD_JPY, bidPrice, askPrice, timestampMS)
        }

        fun usd_cad(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, USD_CAD, bidPrice, askPrice, timestampMS)
        }

        fun aud_usd(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, AUD_USD, bidPrice, askPrice, timestampMS)
        }

        fun gbp_usd(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, GBP_USD, bidPrice, askPrice, timestampMS)
        }


        fun build(source: String, symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote =
            PriceQuote().apply {
                this.source = source
                this.symbol = symbol
                this.askPrice = askPrice
                this.bidPrice = bidPrice
                this.timestampMS = timestampMS
            }
    }
}