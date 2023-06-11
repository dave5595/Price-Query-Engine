package david.demo.common

object Maths {
    private val TENS: LongArray = LongArray(14)

    init {
        TENS[0] = 1;
        for (i in 1 until TENS.size) TENS[i] = 10 * TENS[i - 1]
    }

    fun round(v: Double, precision: Int): Double {
        assert(precision >= 0 && precision < TENS.size)
        val unscaled = v * TENS[precision]
        assert(unscaled > Long.MIN_VALUE && unscaled < Long.MAX_VALUE)
        val unscaledLong = (unscaled + if (v < 0) -0.5 else 0.5).toLong()
        return unscaledLong.toDouble() / TENS[precision]
    }
}