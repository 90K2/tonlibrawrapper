package com.tonlibwrapper.tonlibranext.common.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.math.BigDecimal
import java.math.RoundingMode

class DefaultBigDecimalJsonSerializer(t: Class<BigDecimal>?) : StdSerializer<BigDecimal>(t) {
    override fun serialize(value: BigDecimal?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value?.setScale(2, RoundingMode.DOWN))
    }
}

class TonBigDecimalJsonSerializer(t: Class<BigDecimal>?) : StdSerializer<BigDecimal>(t) {
    override fun serialize(value: BigDecimal?, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeNumber(value?.setScale(9, RoundingMode.DOWN)?.stripTrailingZeros())
    }
}
