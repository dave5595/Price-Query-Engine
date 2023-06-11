package david.demo.data

import java.util.concurrent.ThreadLocalRandom

class BidAskPriceGenerator() {
    private val random = ThreadLocalRandom.current()

    fun generate(minPrice: Double, maxPrice: Double, spread: Double): BidAskPair {
        val bid = random.nextDouble(minPrice, maxPrice)
        val ask = (bid + spread)
        return BidAskPair(bid = bid, ask = ask)
    }

    data class BidAskPair(val bid: Double, val ask: Double)
}