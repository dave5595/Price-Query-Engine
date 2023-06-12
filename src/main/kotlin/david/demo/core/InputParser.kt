package david.demo.core

import david.demo.common.trimAllSpaces

class InputParser(inputValidatorRegex: Regex = SEMI_COLON_SEPARATED_QUERIES_WITH_VALUES_SEPARATED_BY_COMMA_REGEX) {
    private val validator: Validator = Validator(inputValidatorRegex)

    constructor(inputValidatorRegExLiteral: String) : this(inputValidatorRegExLiteral.toRegex())

    fun parse(input: String): List<RawQuery> {
        if (!validator.isValid(input)) throw IllegalArgumentException("Received invalid input format")
        return toQueryStringList(input.trimAllSpaces())
            .map(RawQuery::build)
    }

    class Validator(private val regex: Regex) {
        fun isValid(input: String): Boolean {
            return regex.matches(input)
        }
    }

    companion object {
        //asserts keys as Strings; separated by valid comparators and values that can be strings/(numbers which may be ints or decimals and may be prefixed by -/+ and suffixed by %)
        //e.g of valid input: source=citi,dbs;price<=0.48;pctOffAvgPx>+5.5%
        @JvmField
        val SEMI_COLON_SEPARATED_QUERIES_WITH_VALUES_SEPARATED_BY_COMMA_REGEX =
            "^((([a-zA-Z]+)(\\s+)?(<=|>=|<|>|=)(\\s+)?((\\w+|([+-]?(\\d+)\\.?(\\d+)?%?))(\\s+)?,?(\\s+)?)+)(\\s+)?;?(\\s+)?)+\$".toRegex()

        @JvmStatic
        fun toQueryStringList(queryStr: String): List<String> {
            return queryStr
                .split(";")
                .dropLastWhile { it.isEmpty() }
        }
    }
}