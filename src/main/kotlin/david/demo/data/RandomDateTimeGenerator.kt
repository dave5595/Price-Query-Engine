package david.demo.data

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class RandomDateTimeGenerator(private val startDateTime: LocalDateTime, private val endDateTime: LocalDateTime){
    private val random = Random()
    private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

    fun randomize(): String{
        val startEpoch = startDateTime.toEpochSecond(ZoneOffset.UTC)
        val endEpoch = endDateTime.toEpochSecond(ZoneOffset.UTC)
        val randomEpoch = startEpoch + (random.nextDouble() * (endEpoch - startEpoch))
        val randomDateTime = LocalDateTime.ofEpochSecond(randomEpoch.toLong(), 0, ZoneOffset.UTC)
        return randomDateTime.format(formatter)
    }
}