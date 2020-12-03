@file:UseSerializers(AnySerializer::class)
package com.github.tenebras.otpclient.model

import com.github.tenebras.otpclient.json.AnySerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class Sheet(
    val name: String,
    val rows: List<Map<String, Any?>>
)