package david.demo.core

import java.util.function.Supplier

interface ExternalTimeConsumer<T: ExternalTimeConsumer<T>>{
    fun timeProvider(timeProvider: Supplier<Long>): T
}