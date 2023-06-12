package david.demo.core

import java.util.function.Function
fun interface QueryEngine<I, O> : Function<I, O>