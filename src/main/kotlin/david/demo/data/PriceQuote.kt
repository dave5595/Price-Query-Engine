package david.demo.data

import david.demo.common.SampleCurrencyPairs.AUD_USD
import david.demo.common.SampleCurrencyPairs.EUR_USD
import david.demo.common.SampleCurrencyPairs.USD_CAD
import david.demo.common.SampleCurrencyPairs.GBP_USD
import david.demo.common.SampleCurrencyPairs.USD_JPY
import david.demo.common.SampleSources.CITI
import david.demo.common.SampleSources.DBS
import david.demo.common.SampleSources.REUTERS
import david.demo.common.SampleSources.BARCLAYS
import david.demo.common.SampleSources.UOB

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

        @JvmStatic
        fun byCiti(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(CITI, symbol, bidPrice, askPrice, timestampMS)
        }

        @JvmStatic
        fun byDbs(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(DBS, symbol, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun byReuters(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(REUTERS, symbol, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun byUob(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(UOB, symbol, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun byBarclays(symbol: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(BARCLAYS, symbol, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun eur_usd(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, EUR_USD, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun usd_jpy(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, USD_JPY, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun usd_cad(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, USD_CAD, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun aud_usd(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, AUD_USD, bidPrice, askPrice, timestampMS)
        }
        @JvmStatic
        fun gbp_usd(source: String, bidPrice: Double, askPrice: Double, timestampMS: Long): PriceQuote {
            return build(source, GBP_USD, bidPrice, askPrice, timestampMS)
        }

        @JvmStatic
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