package david.demo.core

import david.demo.common.round3
import david.demo.data.DataLoader
import david.demo.data.PriceQuote
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Predicate
import java.util.function.Supplier


/**
 * Thread-safe implementation of QueryEngine
 * */
class SimplePriceQueryEngine @JvmOverloads constructor(
    priceQuotes: List<PriceQuote> = emptyList(),
    private val timeProvider: Supplier<Long>? = null,
    private val outputType: Output.Type = Output.Type.CSV
) : QueryEngine<String, Output> {
    private val inputParser: InputParser = InputParser()
    private var priceList: MutableList<SidedPrice> = CopyOnWriteArrayList()
    private var avgBidPx: AtomicReference<Double> = AtomicReference(-1.0)
    private var avgAskPx: AtomicReference<Double> = AtomicReference(-1.0)
    private var recalcLock: Any = Any()

    init {
        for (rp in priceQuotes) {
            val priceList = rp.toSidedPriceList()
            updatePriceList(priceList)
        }
    }

    override fun apply(query: String): Output {
        val rawQueries: List<RawQuery> = inputParser.parse(query)
        val predicate: Predicate<SidedPrice> = PredicateBuilder.build(rawQueries, timeProvider)
        return priceList.apply(predicate, outputType)
    }

    fun parseCsv(pathname: String): List<PriceQuote> {
        val priceQuotes: List<PriceQuote> = DataLoader.fromCsv(pathname)
        val newPrices: List<SidedPrice> = priceQuotes.flatMap(PriceQuote::toSidedPriceList)
        updatePriceList(newPrices)
        return priceQuotes
    }

    fun acceptAll(priceQuotes: Array<PriceQuote>) {
        val sidedPrices: List<SidedPrice> = priceQuotes.flatMap(PriceQuote::toSidedPriceList)
        updatePriceList(sidedPrices)
    }

    private fun updatePriceList(sidedPrices: List<SidedPrice>) {
        this.priceList.addAll(sidedPrices)
        this.priceList.recalcPctOffAvgPx()
    }

    private fun recalcSidedAvgPx() {
        avgBidPx.getAndSet(priceList.averagePx(Side.Bid))
        avgAskPx.getAndSet(priceList.averagePx(Side.Ask))
    }

    private fun List<SidedPrice>.recalcPctOffAvgPx() {
        recalcSidedAvgPx()
        synchronized(recalcLock) {
            for (sp in this) {
                sp.pctOffAvgPx = when (sp.side) {
                    Side.Bid -> percentageOffAvgPx(sp.price, avgBidPx.get())
                    Side.Ask -> percentageOffAvgPx(sp.price, avgAskPx.get())
                    else -> throw IllegalArgumentException("Side of ${sp.side} is unsupported")
                }
            }
        }
    }

    companion object {
        private fun List<SidedPrice>.apply(
            predicate: Predicate<SidedPrice>,
            outputType: Output.Type = Output.Type.Table
        ): Output {
            val result: List<SidedPrice> = filter(predicate::test)
            return Output(result, outputType)
        }

        private fun percentageOffAvgPx(price: Double, avgPrice: Double): Double {
            val pxDeviation = price - avgPrice
            val percentageDeviation = pxDeviation / avgPrice
            return (percentageDeviation * 100).round3()
        }

        private fun List<SidedPrice>.averagePx(side: Side): Double {
            return filter { it.side == side }.map { it.price }.average().round3()
        }
    }
}