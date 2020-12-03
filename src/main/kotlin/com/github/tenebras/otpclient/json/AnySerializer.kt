package com.github.tenebras.otpclient.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.ZoneId
import java.util.*

@ExperimentalSerializationApi
class AnySerializer : KSerializer<Any?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("any", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Any? = decoder.decodeString()
    override fun serialize(encoder: Encoder, value: Any?) {

        if (value == null) {
            encoder.encodeNull()
            return
        }



        when (value) {
            is Date -> encoder.encodeString(
                value.toInstant().atZone(ZoneId.systemDefault()).toString()
            )
            is Double -> encoder.encodeDouble(value)
            is Boolean -> encoder.encodeBoolean(value)
            else -> encoder.encodeString(value.toString())
        }
    }
}