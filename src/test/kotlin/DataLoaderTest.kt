import david.demo.common.toCsv
import david.demo.data.DataLoader
import david.demo.data.PriceQuoteGenerator
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class DataLoaderTest {

    @Test
    fun shouldParseCsvCorrectly() {
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

    @Test
    fun shouldConvertBackToTheSameDateString(){
        val date = "20230516093124342"
        val dateInMS = LocalDateTime.parse(date, DataLoader.dtf).toInstant(ZoneOffset.UTC).toEpochMilli()
        val formattedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateInMS), ZoneOffset.UTC).format(DataLoader.dtf)
        assertThat(date).isEqualTo(formattedDate)
    }
}