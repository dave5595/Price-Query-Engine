package david.demo.core

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.function.Supplier

data class SidedPrice @JvmOverloads constructor(
    val source: String,
    val symbol: String,
    val side: Side,
    val price: Double,
    val timestampMS: Long,
    var pctOffAvgPx: Double? = null
) {
    fun age(timeProvider: Supplier<Long>? = null): Long {
        val now = timeProvider?.get() ?: LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        return now - timestampMS
    }
}


