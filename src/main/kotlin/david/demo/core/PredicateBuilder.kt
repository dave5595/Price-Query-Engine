package david.demo.core

import java.util.function.Predicate
import java.util.function.Supplier

class PredicateBuilder {
    companion object {


        @JvmStatic
        @JvmOverloads
        fun build(queryList: List<RawQuery>, timeProvider: Supplier<Long>? = null): Predicate<SidedPrice> {
            return queryList.fold(Predicate<SidedPrice> { true }) { predicate: Predicate<SidedPrice>, rawQuery: RawQuery ->
                if (timeProvider != null && rawQuery is ExternalTimeConsumer<*>) {
                    rawQuery.timeProvider(timeProvider)
                }
                rawQuery.key.applyToPredicate(rawQuery, predicate)
            }
        }
    }
}