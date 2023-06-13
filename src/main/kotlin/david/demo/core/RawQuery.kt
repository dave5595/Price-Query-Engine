package david.demo.core

import david.demo.common.SupportedComparators.equals
import david.demo.common.SupportedComparators.greater
import david.demo.common.SupportedComparators.greaterOrEquals
import david.demo.common.SupportedComparators.lesser
import david.demo.common.SupportedComparators.lesserOrEquals
import david.demo.common.round3
import java.util.function.Predicate
import java.util.function.Supplier

open class RawQuery(val key: Key, val comparator: String, val values: Array<String>) {
    enum class Key(val value: String) {
        Source("source") {
            override val valueValidator: Regex = "^[a-zA-Z]+\$".toRegex()

            override fun apply(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice> {
                return predicate.and(sourceQuery(rawQuery))
            }

            override fun toRawQuery(comparator: String, values: Array<String>): RawQuery {
                return BySource(comparator, values)
            }

            private fun sourceQuery(rawQuery: RawQuery): Predicate<SidedPrice> {
                return rawQuery.values.fold(Predicate<SidedPrice> { false }, ::combineSourceMatchers)
            }

            private fun combineSourceMatchers(
                predicate: Predicate<SidedPrice>,
                value: String
            ): Predicate<SidedPrice> {
                return predicate.or(sourceMatching(value))
            }

            private fun sourceMatching(value: String): Predicate<SidedPrice> {
                return Predicate<SidedPrice> { price -> price.source == value }
            }
        },
        Symbol("symbol") {
            override val valueValidator: Regex = "^([a-zA-Z])+\$".toRegex()

            override fun apply(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice> {
                return predicate.and(symbolQuery(rawQuery))
            }

            override fun toRawQuery(comparator: String, values: Array<String>): RawQuery {
                return BySymbol(comparator, values)
            }

            private fun symbolQuery(rawQuery: RawQuery): Predicate<SidedPrice> {
                return rawQuery.values.fold(Predicate<SidedPrice> { false }, ::combineSymbolMatchers)
            }

            private fun combineSymbolMatchers(
                predicate: Predicate<SidedPrice>,
                value: String
            ): Predicate<SidedPrice> {
                return predicate.or(symbolMatching(value))
            }

            private fun symbolMatching(value: String): Predicate<SidedPrice> {
                return Predicate<SidedPrice> { price -> price.symbol == value }
            }
        },
        Age("age") {
            //TODO support different time suffixes e.g ns,us,s,m,h,d
            override val valueValidator: Regex = "^(\\d+)(ms)?\$".toRegex()

            override fun apply(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice> {
                val timeProvider = (rawQuery as ByAge).timeProvider
                val queriedAge = dropSuffixIfExist(rawQuery.values[0]).toLong()
                return predicate.and(ageQuery(rawQuery.comparator, queriedAge, timeProvider))
            }

            override fun toRawQuery(comparator: String, values: Array<String>): RawQuery {
                return ByAge(comparator, values)
            }

            override fun isMultiValueSupported(): Boolean {
                return false
            }

            private fun dropSuffixIfExist(value: String): String {
                if (!value.endsWith("ms")) return value
                return value.dropLast(2)
            }

            private fun ageQuery(
                comparator: String,
                queriedAge: Long,
                timeProvider: Supplier<Long>? = null
            ): Predicate<SidedPrice> {
                return when (comparator) {
                    greater -> Predicate { price -> price.age(timeProvider) > queriedAge }
                    lesser -> Predicate { price -> price.age(timeProvider) < queriedAge }
                    greaterOrEquals -> Predicate { price -> price.age(timeProvider) >= queriedAge }
                    lesserOrEquals -> Predicate { price -> price.age(timeProvider) <= queriedAge }
                    equals -> Predicate { price -> price.age(timeProvider) == queriedAge }
                    else -> throw IllegalArgumentException("Unsupported comparator of $comparator")
                }
            }
        },
        Price("price") {
            override val valueValidator: Regex = "^[+]?(\\d+)\\.?(\\d+)?\$".toRegex()

            override fun apply(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice> {
                return predicate.and(priceQuery(rawQuery.comparator, rawQuery.values[0]))
            }

            override fun toRawQuery(comparator: String, values: Array<String>): RawQuery {
                return ByPrice(comparator, values)
            }

            override fun isMultiValueSupported(): Boolean {
                return false
            }

            private fun priceQuery(
                comparator: String,
                value: String
            ): Predicate<SidedPrice> {
                return when (comparator) {
                    greater -> Predicate { price -> price.price > value.toDouble() }
                    lesser -> Predicate { price -> price.price < value.toDouble() }
                    greaterOrEquals -> Predicate { price -> price.price >= value.toDouble() }
                    lesserOrEquals -> Predicate { price -> price.price <= value.toDouble() }
                    equals -> Predicate { price -> price.price == value.toDouble() }
                    else -> throw IllegalArgumentException("Unsupported comparator of $comparator")
                }
            }
        },
        PctOffAvgPx("pctOffAvgPx") {
            override val valueValidator: Regex = "^([+-]?(\\d+)\\.?(\\d+)?%?)\$".toRegex()

            override fun apply(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice> {
                return predicate.and(pctFromAvgPxQuery(rawQuery))
            }

            override fun toRawQuery(comparator: String, values: Array<String>): RawQuery {
                return ByPctOffAvgPx(comparator, values)
            }

            private fun dropSuffixIfExist(value: String): String {
                if (!value.endsWith("%")) return value
                return value.dropLast(1)
            }

            override fun isMultiValueSupported(): Boolean {
                return false
            }

            private fun pctFromAvgPxQuery(rawQuery: RawQuery): Predicate<SidedPrice> {
                return pctFromAvgPxMatchers(
                    rawQuery.comparator,
                    dropSuffixIfExist(rawQuery.values[0]).toDouble().round3()
                )
            }

            private fun pctFromAvgPxMatchers(
                comparator: String,
                value: Double
            ): Predicate<SidedPrice> {
                return when (comparator) {
                    greater -> Predicate { it.pctOffAvgPx!! > value }
                    lesser -> Predicate { it.pctOffAvgPx!! < value }
                    greaterOrEquals -> Predicate { it.pctOffAvgPx!! >= value }
                    lesserOrEquals -> Predicate { it.pctOffAvgPx!! <= value }
                    equals -> Predicate { it.pctOffAvgPx!! == value }
                    else -> throw IllegalArgumentException("Unsupported comparator of $comparator")
                }
            }
        };

        fun applyToPredicate(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice> {
            if (rawQuery.values.size > 1 && !isMultiValueSupported()) throw IllegalArgumentException("Multi value querying by ${rawQuery.key} is not permitted")
            if (!areAllValuesValid(rawQuery.values)) throw IllegalArgumentException("One or more values in ${rawQuery.values.contentToString()} are not valid")
            return apply(rawQuery, predicate)
        }


        internal abstract val valueValidator: Regex

        protected abstract fun apply(rawQuery: RawQuery, predicate: Predicate<SidedPrice>): Predicate<SidedPrice>

        abstract fun toRawQuery(comparator: String, values: Array<String>): RawQuery

        protected open fun isMultiValueSupported(): Boolean {
            return true
        }

        protected open fun areAllValuesValid(values: Array<String>): Boolean {
            return values.all(valueValidator::matches)
        }

        companion object {
            private val values = values()
            private val valueMap = values.associateBy(Key::value)
            private val keys = values.map { it.value }

            @JvmStatic
            fun matchingValueOf(value: String): Key {
                val key = keys.firstOrNull { it.equals(value, true) }
                    ?: throw IllegalArgumentException("Key of $value is not supported")
                return valueOfString(key)
            }

            @JvmStatic
            fun valueOfString(value: String): Key {
                return valueMap[value]
                    ?: throw IllegalArgumentException("Key of $value is not supported")
            }
        }
    }

    class ByAge @JvmOverloads constructor(
        comparator: String,
        values: Array<String>,
        var timeProvider: Supplier<Long>? = null
    ) : RawQuery(Key.Age, comparator, values), ExternalTimeConsumer<ByAge> {

        override fun timeProvider(timeProvider: Supplier<Long>): ByAge = apply {
            this.timeProvider = timeProvider
        }
    }

    class BySource(comparator: String, values: Array<String>) : RawQuery(Key.Source, comparator, values)
    class BySymbol(comparator: String, values: Array<String>) : RawQuery(Key.Symbol, comparator, values)
    class ByPrice(comparator: String, values: Array<String>) : RawQuery(Key.Price, comparator, values)
    class ByPctOffAvgPx(comparator: String, values: Array<String>) : RawQuery(Key.PctOffAvgPx, comparator, values)

    companion object {
        private val VALID_COMPARATOR_REGEX = "(=|<=|>=|<|>)".toRegex()

        @JvmStatic
        fun build(query: String): RawQuery {
            val kvList = query.split(VALID_COMPARATOR_REGEX)
            val key = Key.matchingValueOf(kvList[0].lowercase())
            val comparator = extractComparator(query)
            val values = kvList[1].split(",").toTypedArray()
            return key.toRawQuery(comparator, values)
        }

        private fun extractComparator(value: String): String {
            return when {
                value.contains(greaterOrEquals) -> greaterOrEquals
                value.contains(lesserOrEquals) -> lesserOrEquals
                value.contains(lesser) -> lesser
                value.contains(greater) -> greater
                value.contains(equals) -> equals
                else -> throw IllegalArgumentException("Could not detect valid comparator")
            }
        }
    }
}



