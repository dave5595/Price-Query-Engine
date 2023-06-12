package david.demo.core

import david.demo.common.round3
import david.demo.data.DataLoader
import david.demo.data.PriceQuote
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Predicate
import java.util.function.Supplier


/**
 * Thread-safe Implementation of QueryEngine
 *  Filter prices for input symbol
 *  Filter outliers that are more than x% off the average.
 *  Filter prices that are more than X milli-seconds old (time stamp &gt; now - X)
 *  Filter based on source system.
 * */
class SimplePriceQueryEngine @JvmOverloads constructor(
    priceQuotes: List<PriceQuote> = emptyList(),
    private val outputType: Output.Type = Output.Type.CSV
) : QueryEngine<String, Output> {
    private val parser = InputParser()
    private var priceList: MutableList<SidedPrice> = CopyOnWriteArrayList()
    private var timeProvider: Supplier<Long>? = null
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
        val rawQueries: List<RawQuery> = parser.parse(query)
        val predicate: Predicate<SidedPrice> = PredicateBuilder.build(rawQueries, timeProvider)
        return priceList.apply(predicate, outputType)
    }

    fun parseCsv(pathname: String): List<PriceQuote> {
        val priceQuotes = DataLoader.fromCsv(pathname)
        val newPrices = priceQuotes.flatMap(PriceQuote::toSidedPriceList)
        updatePriceList(newPrices)
        return priceQuotes
    }

    fun withTimeProvider(timeProvider: Supplier<Long>): SimplePriceQueryEngine = apply {
        this.timeProvider = timeProvider
    }

    fun acceptAll(priceQuotes: Array<PriceQuote>) {
        val sidedPrices = priceQuotes.flatMap(PriceQuote::toSidedPriceList)
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
                    Side.Bid -> SidedPrice.percentageOffAvgPx(sp.price, avgBidPx.get())
                    Side.Ask -> SidedPrice.percentageOffAvgPx(sp.price, avgAskPx.get())
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
            val result = filter(predicate::test)
            return Output(result, outputType)
        }

        private fun List<SidedPrice>.averagePx(side: Side): Double {
            return filter { it.side == side }.map { it.price }.average().round3()
        }
    }
}