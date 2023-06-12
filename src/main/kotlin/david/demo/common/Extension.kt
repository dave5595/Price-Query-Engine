@file:JvmName("Utils")

package david.demo.common

import david.demo.data.DataLoader
import david.demo.data.PriceQuote
import java.io.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@JvmOverloads
fun String.padBothSides(totalWidth: Int, paddingChar: Char = ' '): String {
    val padding = totalWidth - this.length
    val leftPadding = padding / 2
    val rightPadding = padding - leftPadding
    return paddingChar.toString().repeat(leftPadding) + this + paddingChar.toString().repeat(rightPadding)
}

fun String.trimAllSpaces(): String {
    return replace("\\s+".toRegex(), "")
}

fun Double.round(precision: Int): Double {
    return Maths.round(this, precision)
}

fun Double.round3(): Double {
    return Maths.round(this, 3)
}

@JvmOverloads
fun formatToString(
    timestampMS: Long,
    dtf: DateTimeFormatter = DataLoader.dtf
): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMS), ZoneOffset.UTC)
    return dateTime.format(dtf)
}

fun Array<PriceQuote>.toCsv(filePath: String) {
    try {
        FileWriter(filePath).use { writer ->
            // Write the CSV header
            writer.write("source,symbol,timestamp,bidPrice,askPrice\n")
            // Write each object to the CSV file
            for (quote in this) {
                val line =
                    "${quote.source},${quote.symbol},${formatToString(quote.timestampMS)},${quote.bidPrice},${quote.askPrice}\n"
                writer.write(line)
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

