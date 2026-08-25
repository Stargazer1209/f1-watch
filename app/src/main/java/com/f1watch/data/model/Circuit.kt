package com.f1watch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Circuit(
    @SerialName("circuitId") val circuitId: String = "",
    @SerialName("circuitName") val circuitName: String = "",
    @SerialName("locality") val locality: String = "",
    @SerialName("country") val country: String = "",
    @SerialName("countryCode") val countryCode: String = "",
    @SerialName("flagEmoji") val flagEmoji: String = "🏁",
    @SerialName("lat") val lat: Double = 0.0,
    @SerialName("long") val long: Double = 0.0
)
