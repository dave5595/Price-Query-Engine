import david.demo.common.SupportedCurrencyPairs.AUD_USD
import david.demo.common.SupportedCurrencyPairs.EUR_USD
import david.demo.common.SupportedCurrencyPairs.USD_CAD
import david.demo.common.SupportedCurrencyPairs.USD_JPY
import david.demo.common.toCsv
import david.demo.core.Output
import david.demo.core.SidedPrice
import david.demo.core.SimplePriceQueryEngine
import david.demo.data.PriceQuote
import david.demo.data.PriceQuoteGenerator
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.function.Function

/**
 *  Filter prices for input symbol
 *  Filter outliers that are more than x% off the average.
 *  Filter prices that are more than X milli-seconds old (time stamp &gt; now - X)
 *  Filter based on source system.*/
class SimplePriceQueryEngineTest {

    @Test
    fun shouldBeAbleToLoadDataStraightFromCsvFile() {
        val inputPath = "input-test.csv"
        val quoteGenerator = PriceQuoteGenerator()
        val priceQuotes = quoteGenerator.generate(100)
        priceQuotes.toCsv(inputPath)

        val queryEngine = SimplePriceQueryEngine()
        val priceQuotes0 = queryEngine.parseCsv(inputPath)
        assertThatCollection(priceQuotes0).hasSize(100)
        File(inputPath).delete()
    }

    @Test
    fun outputBidAndAskShouldBeSortedInDescGivenAnUnsortedListO() {
        val nowMS = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.068, nowMS), //-1.395% , -0.928%
            PriceQuote.byReuters(EUR_USD, 1.056, 1.065, nowMS - 5), //-1.767%, -1.206%
            PriceQuote.byDbs(EUR_USD, 1.057, 1.058, nowMS - 10), //-1.674%, -1.855%
            PriceQuote.byDbs(EUR_USD, 1.056, 1.057, nowMS - 15), // -1.767%, -1.948%
            PriceQuote.byDbs(EUR_USD, 1.059, 1.061, nowMS - 20), // -1.488%, -1.577%
            PriceQuote.byReuters(EUR_USD, 1.065, 1.066, nowMS - 50), //-0.93%, -1.113%
            PriceQuote.byBarclays(EUR_USD, 1.061, 1.062, nowMS - 49), // -1.302%,-1.484%
            PriceQuote.byBarclays(EUR_USD, 1.183, 1.186, nowMS - 100), // 10.047%,10.019%
        )
        val priceList = priceQuotes.flatMap(PriceQuote::toSidedPriceList)
        val output = Output(priceList)
        assertThat(output.bids).isSortedAccordingTo(Comparator.comparingDouble(SidedPrice::price).reversed())
        assertThat(output.asks).isSortedAccordingTo(Comparator.comparingDouble(SidedPrice::price).reversed())
    }

    /**
     * Application only supports:
     * conjunction queries where x >= lower bound AND x <=upper bound
     * disjunction queries. e.g x <= lower bound OR x >= upper bound is not supported atm
     * */
    @Test
    fun shouldNotYieldExpectedResultsWhenProcessingDisjunctiveQueries() {
        val nowMS = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

        //avgBidPx = 1.075
        //avgAskPx = 1.078
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.068, nowMS), //-1.395% , -0.928%
            PriceQuote.byReuters(EUR_USD, 1.056, 1.065, nowMS - 5), //-1.767%, -1.206%
            PriceQuote.byDbs(EUR_USD, 1.057, 1.058, nowMS - 10), //-1.674%, -1.855%
            PriceQuote.byDbs(EUR_USD, 1.056, 1.057, nowMS - 15), // -1.767%, -1.948%
            PriceQuote.byDbs(EUR_USD, 1.059, 1.061, nowMS - 20), // -1.488%, -1.577%
            PriceQuote.byReuters(EUR_USD, 1.065, 1.066, nowMS - 50), //-0.93%, -1.113%
            PriceQuote.byBarclays(EUR_USD, 1.061, 1.062, nowMS - 49), // -1.302%,-1.484%
            PriceQuote.byBarclays(EUR_USD, 1.183, 1.186, nowMS - 100), // 10.047%,10.019%
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)

        val result = priceQueryEngine
            .withTimeProvider { nowMS }
            .apply("pctOffAvgPx < -1.5;pctOffAvgPx >= 10.0195")
        assertThatExceptionOfType(AssertionError::class.java)
            .isThrownBy {
                assertThatCollection(result.prices)
                    .map(Function { it.source })
                    .containsOnly("barclays", "dbs", "reuters")

                assertThatCollection(result.prices)
                    .map(Function { it.price })
                    .containsOnly(1.183, 1.061, 1.056, 1.057, 1.057, 1.058, 1.056)

                assertThat(result.asks).hasSize(3)
                assertThat(result.bids).hasSize(4)
            }

        println(result)
    }

    @Test
    fun shouldYieldExpectedResultsWhenProcessingConjunctiveQueries() {
        val nowMS = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

        //avgBidPx = 1.075
        //avgAskPx = 1.078
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.068, nowMS), //-1.395% , -0.928%
            PriceQuote.byReuters(EUR_USD, 1.056, 1.065, nowMS - 5), //-1.767%, -1.206%
            PriceQuote.byDbs(EUR_USD, 1.057, 1.058, nowMS - 10), //-1.674%, -1.855%
            PriceQuote.byDbs(EUR_USD, 1.056, 1.057, nowMS - 15), // -1.767%, -1.948%
            PriceQuote.byDbs(EUR_USD, 1.059, 1.061, nowMS - 20), // -1.488%, -1.577%
            PriceQuote.byReuters(EUR_USD, 1.065, 1.066, nowMS - 50), //-0.93%, -1.113%
            PriceQuote.byBarclays(EUR_USD, 1.061, 1.062, nowMS - 49), // -1.302%,-1.484%
            PriceQuote.byBarclays(EUR_USD, 1.183, 1.186, nowMS - 100), // 10.047%,10.019%
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        //values< -1.2 || values >5%
        //values> -1.2 && values <5%
        val result = priceQueryEngine
            .withTimeProvider { nowMS }
            .apply("pctOffAvgPx > -1.5 ; pctOffAvgPx <= 10.0195; ")
        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("citi", "barclays", "dbs", "reuters")

        assertThatCollection(result.prices)
            .map(Function { it.price })
            .containsOnly(1.186, 1.061, 1.062, 1.065, 1.066, 1.059, 1.068, 1.06)

        assertThat(result.asks).hasSize(5)
        assertThat(result.bids).hasSize(4)
        println(result)
    }

    @Test
    fun shouldHandleMultiSourceQuerySortingResultsInDescOrder() {
        val now = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, now),
            PriceQuote.byReuters(EUR_USD, 1.056, 1.065, now),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, now),
            PriceQuote.byReuters(USD_CAD, 2.957, 3.959, now),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine.apply("source=citi,reuters")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("citi", "reuters")

        assertThat(result.asks).hasSize(3)
        assertThat(result.bids).hasSize(3)
        println(result)
    }

    @Test
    fun shouldHandleSymbolQuerySortingResultsInDescOrder() {
        val now = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, now),
            PriceQuote.byReuters(USD_JPY, 1.056, 1.065, now),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, now),
            PriceQuote.byReuters(EUR_USD, 3.859, 3.959, now),
            PriceQuote.byBarclays(USD_JPY, 2.94, 3.95, now),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, now),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine.apply("symbol=USDJPY;")

        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("USDJPY")

        assertThat(result.asks).hasSize(3)
        assertThat(result.bids).hasSize(3)
        println(result)
    }

    @Test
    fun shouldHandlePriceConjunctiveQuerySortingResultsInDescOrder() {
        val now = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, now),
            PriceQuote.byReuters(USD_JPY, 1.056, 1.065, now),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, now),
            PriceQuote.byReuters(EUR_USD, 3.859, 3.959, now),
            PriceQuote.byBarclays(USD_JPY, 2.94, 3.95, now),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, now),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine.apply("price>2.5;price<=3")

        assertThatCollection(result.prices)
            .map(Function { it.price })
            .allMatch { it > 2.5 && it <= 3 }

        assertThat(result.asks).hasSize(3)
        assertThat(result.bids).hasSize(5)
        println(result)
    }

    @Test
    fun shouldHandleAgeQuerySortingResultsInDescOrder() {
        val nowMS = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        //higher number means younger// lower number means older
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, nowMS),
            PriceQuote.byReuters(USD_JPY, 1.056, 1.065, nowMS - 5),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, nowMS - 10),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, nowMS - 15),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, nowMS - 20),
            PriceQuote.byReuters(EUR_USD, 3.859, 3.959, nowMS - 50),
            PriceQuote.byBarclays(USD_JPY, 2.94, 3.95, nowMS - 49),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, nowMS - 100),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine
            .withTimeProvider { nowMS }
            .apply("age >= 50ms")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("reuters", "barclays")

        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("EURUSD")

        assertThatCollection(result.prices)
            .map(Function { it.price })
            .containsOnly(3.859, 3.959, 2.956, 3.957)

        assertThat(result.asks).hasSize(2)
        assertThat(result.bids).hasSize(2)
        println(result)
    }

    //                            (50ms)          (51ms)        (49ms)
    // given an age list of [1686486764230, 1686486764229, 1686486764231]; now = 1686486764280; and derivedQueryTimestamp = 1686486764230 (now - 50)
    // when age query be applied where age <= 50ms;
    // then filtered age should be [1686486764230, 1686486764231]
    @Test
    fun shouldHandleAgeConjunctiveQuerySortingResultsInDescOrder() {
        val nowMS = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, nowMS),
            PriceQuote.byReuters(USD_JPY, 1.056, 1.065, nowMS - 5),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, nowMS - 10),
            PriceQuote.byDbs(USD_JPY, 2.957, 3.958, nowMS - 15),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, nowMS - 20),
            PriceQuote.byReuters(EUR_USD, 3.859, 3.959, nowMS - 50),
            PriceQuote.byBarclays(USD_JPY, 2.941, 3.942, nowMS - 49),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, nowMS - 100),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine
            .withTimeProvider { nowMS }
            .apply("age > 5ms; age < 50ms")
        //values< -1.2 || values >5%
        //values> -1.2 && values <5%
        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("dbs", "barclays")

        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("EURUSD", "USDJPY", "USDCAD")

        assertThatCollection(result.prices)
            .map(Function { it.price })
            .containsOnly(2.956, 3.000, 2.957, 3.958, 2.93, 2.94, 2.941, 3.942)

        assertThat(result.asks).hasSize(4)
        assertThat(result.bids).hasSize(4)
        println(result)
    }

    @Test
    fun shouldHandlePctOffAvgPxQuerySortingResultsInDescOrder() {
        val nowMS = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

        //avgBidPx = 1.075
        //avgAskPx = 1.078
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.068, nowMS), //-1.395% , -0.928%
            PriceQuote.byReuters(EUR_USD, 1.056, 1.065, nowMS - 5), //-1.767%, -1.206%
            PriceQuote.byDbs(EUR_USD, 1.057, 1.058, nowMS - 10), //-1.674%, -1.855%
            PriceQuote.byDbs(EUR_USD, 1.056, 1.057, nowMS - 15), // -1.767%, -1.948%
            PriceQuote.byDbs(EUR_USD, 1.059, 1.061, nowMS - 20), // -1.488%, -1.577%
            PriceQuote.byReuters(EUR_USD, 1.065, 1.066, nowMS - 50), //-0.93%, -1.113%
            PriceQuote.byBarclays(EUR_USD, 1.061, 1.062, nowMS - 49), // -1.302%,-1.484%
            PriceQuote.byBarclays(EUR_USD, 1.183, 1.186, nowMS - 100), // 10.047%,10.019%
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)

        val result = priceQueryEngine
            .apply("pctOffAvgPx >= 10.019%")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("barclays")

        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("EURUSD")

        assertThatCollection(result.prices)
            .map(Function { it.price })
            .containsOnly(1.183, 1.186)

        assertThat(result.asks).hasSize(1)
        assertThat(result.bids).hasSize(1)
        println(result)
    }

    @Test
    fun shouldHandlePctOffAvgPxAndAgeQuerySortingResultsInDescOrder() {
        val nowMS = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

        //avgBidPx = 1.075
        //avgAskPx = 1.078
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.068, nowMS), //-1.395% , -0.928%
            PriceQuote.byReuters(EUR_USD, 1.056, 1.065, nowMS - 5), //-1.767%, -1.206%
            PriceQuote.byDbs(EUR_USD, 1.057, 1.058, nowMS - 10), //-1.674%, -1.855%
            PriceQuote.byDbs(EUR_USD, 1.056, 1.057, nowMS - 15), // -1.767%, -1.948%
            PriceQuote.byDbs(EUR_USD, 1.059, 1.061, nowMS - 20), // -1.488%, -1.577%
            PriceQuote.byReuters(EUR_USD, 1.065, 1.066, nowMS - 50), //-0.93%, -1.113%
            PriceQuote.byBarclays(EUR_USD, 1.061, 1.062, nowMS - 49), // -1.302%,-1.484%
            PriceQuote.byBarclays(EUR_USD, 1.183, 1.186, nowMS - 100), // 10.047%,10.019%
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)

        val result = priceQueryEngine
            .withTimeProvider { nowMS }
            .apply("pctOffAvgPx >= 10.0195%; age >=50 ")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("barclays")

        assertThatCollection(result.prices)
            .map(Function { it.price })
            .containsOnly(1.183)

        assertThat(result.asks).hasSize(0)
        assertThat(result.bids).hasSize(1)
        println(result)
    }

    @Test
    fun shouldHandleSourceAndSymbolQuerySortingResultsInDescOrder() {
        val now = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, now),
            PriceQuote.byReuters(AUD_USD, 1.056, 1.065, now),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, now),
            PriceQuote.byReuters(EUR_USD, 2.957, 3.959, now),
            PriceQuote.byBarclays(USD_CAD, 2.94, 3.95, now),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, now),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine.apply("source=citi,reuters,barclays;symbol=EURUSD,USDCAD")
        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("EURUSD", "USDCAD")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("citi", "reuters", "barclays")

        assertThat(result.asks).hasSize(4)
        assertThat(result.bids).hasSize(4)
        println(result)
    }

    @Test
    fun shouldHandleSourceAndPriceQuerySortingResultsInDescOrder() {
        val now = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, now),
            PriceQuote.byReuters(AUD_USD, 1.056, 1.065, now),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, now),
            PriceQuote.byReuters(EUR_USD, 3.859, 3.959, now),
            PriceQuote.byBarclays(USD_CAD, 2.94, 3.95, now),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, now),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine.apply("source=dbs,reuters,barclays;price < 3.0")
        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("EURUSD", "USDJPY", "USDCAD", "AUDUSD")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("dbs", "reuters", "barclays")

        assertThat(result.asks).hasSize(2)
        assertThat(result.bids).hasSize(6)
        println(result)
    }

    @Test
    fun shouldHandleSourceAndPriceQueryWithRepeatingGroupsSortingResultsInDescOrder() {
        val now = System.currentTimeMillis()
        val priceQuotes = listOf(
            PriceQuote.byCiti(EUR_USD, 1.06, 1.07, now),
            PriceQuote.byReuters(AUD_USD, 1.056, 1.065, now),
            PriceQuote.byDbs(EUR_USD, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_JPY, 2.956, 3.000, now),
            PriceQuote.byDbs(USD_CAD, 2.93, 2.94, now),
            PriceQuote.byReuters(EUR_USD, 3.859, 3.959, now),
            PriceQuote.byBarclays(USD_CAD, 2.94, 3.95, now),
            PriceQuote.byBarclays(EUR_USD, 2.956, 3.957, now),
        )
        val priceQueryEngine = SimplePriceQueryEngine(priceQuotes)
        val result = priceQueryEngine.apply("source=dbs,reuters,barclays;price>=2.0;price < 3.0;")
        assertThatCollection(result.prices)
            .map(Function { it.symbol })
            .containsOnly("EURUSD", "USDJPY", "USDCAD")

        assertThatCollection(result.prices)
            .map(Function { it.source })
            .containsOnly("dbs", "barclays")

        assertThat(result.asks).hasSize(1)
        assertThat(result.bids).hasSize(5)
        println(result)
    }
}