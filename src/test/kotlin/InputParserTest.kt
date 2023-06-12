import david.demo.common.round
import david.demo.common.round3
import david.demo.core.InputParser
import david.demo.core.InputParser.Companion.SEMI_COLON_SEPARATED_QUERIES_WITH_VALUES_SEPARATED_BY_COMMA_REGEX
import david.demo.core.InputParser.Companion.toQueryStringList
import david.demo.core.PredicateBuilder
import david.demo.core.RawQuery
import david.demo.core.SidedPrice
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InputParserTest {

    @Test
    fun shouldAssertValidInputStringFormatCorrectly() {
        val validator = InputParser.Validator(
            regex = SEMI_COLON_SEPARATED_QUERIES_WITH_VALUES_SEPARATED_BY_COMMA_REGEX
        )
        assertThat(validator.isValid("source=reuters")).isTrue
        assertThat(validator.isValid("source=reuters,citi,uob;")).isTrue
        assertThat(validator.isValid("source=reuters,citi,uob")).isTrue
        assertThat(validator.isValid("source=reuters,citi,uob;symbol=EURUSD,USDJPY")).isTrue
        assertThat(validator.isValid("symbol=EURUSD;pctOffAvgPx<=10%,-5%;")).isTrue
        assertThat(validator.isValid("source=reuters;symbol=EURUSD;age<=100")).isTrue
        assertThat(validator.isValid("SOURCE=REUTERS")).isTrue
        assertThat(validator.isValid("SOURCE=REUTERS;")).isTrue
        assertThat(validator.isValid("SOURCE=REUTERS;")).isTrue
        assertThat(validator.isValid("SOURCE=REUTERS;age<=10")).isTrue
        assertThat(validator.isValid("SOURCE = reuters ; age <= 10 ;")).isTrue
        assertThat(validator.isValid("SOURCE = reuters;\nage <= 10 ;")).isTrue
        assertThat(validator.isValid("sOuRcE=ReUtErS;AgE<=10;")).isTrue
        assertThat(validator.isValid("sOuRcE=ReUtErS;AgE<=10.0;")).isTrue
        assertThat(validator.isValid("sOuRcE=ReUtErS;price<=-10.0;")).isTrue
        assertThat(validator.isValid("sOuRcE=ReUtErS;price<=+10.0;")).isTrue
        assertThat(validator.isValid("source=reuters;")).isTrue
        assertThat(validator.isValid("source=citi,dbs;price<=0.48;PCTOFFAVGPX>+5.5%")).isTrue
        assertThat(validator.isValid("source=reuters/citi/uob;")).isFalse
        assertThat(validator.isValid("source==reuters")).isFalse
        assertThat(validator.isValid("98=citi,dbs;price<=0.48;percOutliers>+5.5%")).isFalse
        assertThat(validator.isValid("")).isFalse
        assertThat(validator.isValid(";")).isFalse
        assertThat(validator.isValid(";source=reuters")).isFalse
        assertThat(validator.isValid("source=reuters,citi,uob;;symbol=EURUSD,USDJPY")).isFalse
        assertThat(validator.isValid(";source=reuters;symbol=EURUSD;age<=100")).isFalse
        assertThat(validator.isValid("source=reuters=")).isFalse
        assertThat(validator.isValid("source=reuters?")).isFalse
        assertThat(validator.isValid("source=?")).isFalse
        assertThat(validator.isValid("source=")).isFalse
        assertThat(validator.isValid("source")).isFalse
        assertThat(validator.isValid("SOURCE=REUTERS;age<=10>")).isFalse
        assertThat(validator.isValid("SOURCE=REUTERS;age<=10\"")).isFalse
    }

    @Test
    fun sourceValueValidatorShouldAllowLettersOnly() {
        assertThat(RawQuery.Key.Source.valueValidator.matches("dbs")).isTrue
        assertThat(RawQuery.Key.Source.valueValidator.matches("DBS")).isTrue
        assertThat(RawQuery.Key.Source.valueValidator.matches("DB5")).isFalse
        assertThat(RawQuery.Key.Source.valueValidator.matches(" DBS ")).isFalse
    }

    @Test
    fun symbolValueValidatorShouldAllowLettersOnly() {
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("EURUSD")).isTrue
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("usdjpy")).isTrue
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("EUR_JPY")).isFalse
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("")).isFalse
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("EUR_")).isFalse
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("_EUR")).isFalse
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("EUR-JPY")).isFalse
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("EURU5D")).isFalse
        assertThat(RawQuery.Key.Symbol.valueValidator.matches("_EURUSD_")).isFalse
    }

    @Test
    fun ageValueValidatorShouldAllowPositiveIntegersWithOptionalMSSuffix() {
        assertThat(RawQuery.Key.Age.valueValidator.matches("100")).isTrue
        assertThat(RawQuery.Key.Age.valueValidator.matches("87ms")).isTrue
        assertThat(RawQuery.Key.Age.valueValidator.matches("87m")).isFalse
        assertThat(RawQuery.Key.Age.valueValidator.matches("87s")).isFalse
        assertThat(RawQuery.Key.Age.valueValidator.matches("+100")).isFalse
        assertThat(RawQuery.Key.Age.valueValidator.matches("-100")).isFalse
        assertThat(RawQuery.Key.Age.valueValidator.matches("99.9")).isFalse
        assertThat(RawQuery.Key.Age.valueValidator.matches("")).isFalse
        assertThat(RawQuery.Key.Age.valueValidator.matches("ten")).isFalse
    }


    @Test
    fun priceValueValidatorShouldAllowDecimalsOrIntegersOnly() {
        assertThat(RawQuery.Key.Price.valueValidator.matches("0.987213")).isTrue
        assertThat(RawQuery.Key.Price.valueValidator.matches("87")).isTrue
        assertThat(RawQuery.Key.Price.valueValidator.matches("$9.8")).isFalse
        assertThat(RawQuery.Key.Price.valueValidator.matches(".987213")).isFalse
        assertThat(RawQuery.Key.Price.valueValidator.matches("0.987213usd")).isFalse
        assertThat(RawQuery.Key.Price.valueValidator.matches("1dollar")).isFalse
    }

    @Test
    fun percentageOffAvgPriceValueValidatorShouldAllowNumbersWithOptionalPercentageCharAsSuffix() {
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("3")).isTrue
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("10%")).isTrue
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("3.0%")).isTrue
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("+1")).isTrue
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("-0.131")).isTrue
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches(".5%")).isFalse
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("")).isFalse
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("%")).isFalse
        assertThat(RawQuery.Key.PctOffAvgPx.valueValidator.matches("3.0e%")).isFalse
    }

    @Test
    fun shouldDropTrailingSemiColon() {
        val queryList = toQueryStringList("SOURCE=REUTERS;")
        assertThat(queryList.size).isEqualTo(1)
        println(queryList)
    }

    @Test
    fun shouldParseValidQueryWithNoExceptions() {
        Assertions
            .assertThatNoException()
            .isThrownBy { parser.parse("SOURCE=reuters,citi;symbol=EURUSD,USDJPY;age<=100;pctOffAvgPx>=10%") }
    }



    @Test
    fun shouldHandleRepeatingQueryGroups() {
        Assertions
            .assertThatNoException()
            .isThrownBy {
                val queries =
                    parser.parse("SOURCE=reuters,citi;symbol=EURUSD,USDJPY;age<=100ms;age>5ms;pctOffAvgPx>=10%;pctOffAvgPx>=-10%")
                PredicateBuilder.build(queries)
            }
    }

    @Test
    fun shouldThrowExceptionForKeysThatDoesNotSupportMultiValues() {
        Assertions
            .assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { PredicateBuilder.build(parser.parse("SOURCE=reuters;symbol=EURUSD;age<=100,10ms;pctOffAvgPx>=10%,-10%")) }
            .withMessage("Multi value querying by Age is not permitted")
        Assertions
            .assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { PredicateBuilder.build(parser.parse("SOURCE=reuters;symbol=EURUSD;pctOffAvgPx>=10%,-10%;price<=0.98,10;age<=100,10ms")) }
            .withMessage("Multi value querying by Price is not permitted")
    }


    companion object {
        private val parser = InputParser()
    }

}






