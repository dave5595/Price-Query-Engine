package david.demo.core

import david.demo.common.formatToString
import david.demo.common.padBothSides
import java.util.function.Consumer
import java.util.function.Function

class Output @JvmOverloads constructor(val prices: List<SidedPrice>? = null, private val outputType: Type = Type.CSV) :
    Consumer<SidedPrice>, Function<Output, Output> {
    val bids: MutableList<SidedPrice> = ArrayList()
    val asks: MutableList<SidedPrice> = ArrayList()

    init {
        prices?.also { pl ->
            for (price in pl) {
                when (price.side) {
                    Side.Bid -> bids.add(price)
                    Side.Ask -> asks.add(price)
                    else -> throw java.lang.IllegalArgumentException("Price side of ${price.side} is not supported")
                }
            }
        }
        bids.sortByDescending { it.price }
        asks.sortByDescending { it.price }
    }

    override fun apply(pb: Output): Output {
        bids.addAll(pb.bids)
        asks.addAll(pb.bids)
        return this
    }

    override fun accept(rp: SidedPrice) {
        add(rp)
    }

    override fun toString(): String {
        return when (outputType) {
            Type.Table -> toTable()
            Type.CSV -> toCsv()
        }

    }

    private fun toTable(): String {
        val sb = StringBuilder()
        val columnWidth = 64
        val bidHeader = "Bids"
        val askHeader = "Asks"
        val bidHeaderFormatted = String.format("%s", bidHeader).padBothSides(columnWidth)
        val askHeaderFormatted = String.format("%s", askHeader).padBothSides(columnWidth)
        val topHeader = "|\t$bidHeaderFormatted|$askHeaderFormatted\t|\n"
        val bottomHeader = "|\t%-10s\t%-10s\t%-5s\t%-10s\t%-17s\t|\t%-10s\t%-10s\t%-5s\t%-10s\t%-17s\t|\n".format(
            "Source", "Symbol", "Side", "Price", "Timestamp", "Source", "Symbol", "Side", "Price", "Timestamp"
        )
        val divider = "-".repeat(topHeader.length + 3 /*for tabs*/) + "\n"
        val maxSize = maxOf(bids.size, asks.size)

        sb.append(divider)
        sb.append(topHeader)
        sb.append(divider)
        sb.append(bottomHeader)
        sb.append(divider)
        for (i in 0 until maxSize) {
            val bid = if (i < bids.size) bids[i] else SidedPrice("null", "null", Side.None, -1.0, -1)
            val ask = if (i < asks.size) asks[i] else SidedPrice("null", "null", Side.None, -1.0, -1)
            sb.append(
                "|\t%-10s\t%-10s\t%-5s\t%-10.3f\t%-17s\t|\t%-10s\t%-10s\t%-5s\t%-10.3f\t%-17s\t|\n".format(
                    bid.source, bid.symbol, bid.side, bid.price, formatToString(bid.timestampMS),
                    ask.source, ask.symbol, ask.side, ask.price, formatToString(ask.timestampMS)
                )
            )
        }
        sb.append(divider)

        return sb.toString()
    }

    private fun toCsv(): String {
        val sb = StringBuilder()
        val header = "bidSource,bidTimestamp,bidprice,askSource,askTimestamp,askPrice\n"
        val maxSize = maxOf(bids.size, asks.size)
        sb.append(header)
        for (i in 0 until maxSize) {
            val bid = if (i < bids.size) bids[i] else SidedPrice("null", "null", Side.None, -1.0, -1)
            val ask = if (i < asks.size) asks[i] else SidedPrice("null", "null", Side.None, -1.0, -1)
            val line = "${bid.source},${formatToString(bid.timestampMS)},${bid.price}," +
                    "${ask.source},${formatToString(ask.timestampMS)},${ask.price},\n"
            sb.append(line)
        }
        return sb.toString()
    }

    private fun add(price: SidedPrice) {
        when (price.side) {
            Side.Bid -> bids.add(price)
            Side.Ask -> asks.add(price)
            else -> throw IllegalArgumentException("Side of ${price.side} is not supported")
        }
    }

    enum class Type {
        Table, CSV
    }


}