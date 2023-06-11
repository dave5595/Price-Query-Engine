import david.demo.common.toCsv
import david.demo.data.DataLoader
import david.demo.data.PriceQuoteGenerator
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class DataLoaderTest {

    @Test
    fun shouldParseCsvCorrectly(){
        val pathName = "test-output.csv"
        val quoteGenerator = PriceQuoteGenerator()
        val quotes0 = quoteGenerator.generate(151)
        quotes0.toCsv(pathName)
        //try loading data

        Assertions.assertThatNoException().isThrownBy {
            val quotes = DataLoader.fromCsv(pathName)
            assertThat(quotes.size).isEqualTo(quotes0.size)
            quotes.forEach(::println)
        }
        File(pathName).delete()
    }
}