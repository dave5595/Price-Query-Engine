package david.demo.core

import david.demo.common.round3

data class SidedPrice(
    val source: String,
    val symbol: String,
    val side: Side,
    val price: Double,
    val timestampMS: Long
) {
    var pctOffAvgPx: Double? = null

    fun setPctOffAvgPx(bidAvg: Double, askAvg: Double) = apply {
        pctOffAvgPx = when (side) {
            Side.Bid -> percentageOffAvgPx(price, bidAvg)
            Side.Ask -> percentageOffAvgPx(price, askAvg)
            else -> throw IllegalArgumentException("Side of $side is unsupported")
        }
    }

    private fun percentageOffAvgPx(price: Double, avgPrice: Double): Double {
        val pxDeviation = price - avgPrice
        val percentageDeviation = pxDeviation / avgPrice
        return (percentageDeviation * 100).round3()
    }
}

enum class Side { Bid, Ask, None }
