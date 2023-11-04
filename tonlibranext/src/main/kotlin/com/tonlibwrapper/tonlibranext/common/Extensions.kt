package com.tonlibwrapper.tonlibranext.common

import java.math.BigDecimal

object Extensions {

    fun Double.toNano(): Long = (this * NANOCOIN).toLong()

    fun Int.toNano(): Long = (this * NANOCOIN).toLong()

    fun Long.fromNano() = BigDecimal(this)
            .divide(BigDecimal(NANOCOIN))

    fun BigDecimal.toNano(): Long = (this * NANOCOIN_BIG_DECIMAL).toLong()


    fun Int.toBinary(len: Int): String {
        return String.format("%" + len + "s", this.toString(2)).replace(" ".toRegex(), "0")
    }

    fun String.toBinaryArray(): List<Boolean> {
        return this.map { it != Char(48) }
    }

}
