package david.demo.core

import david.demo.common.round3

data class SidedPrice @JvmOverloads constructor(
    val source: String,
    val symbol: String,
    val side: Side,
    val price: Double,
    val timestampMS: Long,
    var pctOffAvgPx: Double? = null
) {

    companion object{
        @JvmStatic
        fun percentageOffAvgPx(price: Double, avgPrice: Double): Double {
            val pxDeviation = price - avgPrice
            val percentageDeviation = pxDeviation / avgPrice
            return (percentageDeviation * 100).round3()
        }
    }
}


