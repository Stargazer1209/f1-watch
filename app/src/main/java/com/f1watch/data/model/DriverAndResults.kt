package com.f1watch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Driver(
    @SerialName("driverId") val driverId: String = "",
    @SerialName("permanentNumber") val permanentNumber: String = "",
    @SerialName("code") val code: String = "",
    @SerialName("givenName") val givenName: String = "",
    @SerialName("familyName") val familyName: String = "",
    @SerialName("dateOfBirth") val dateOfBirth: String = "",
    @SerialName("nationality") val nationality: String = ""
) {
    val fullName: String
        get() = "$givenName $familyName"
}

@Serializable
data class Constructor(
    @SerialName("constructorId") val constructorId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("nationality") val nationality: String = ""
)

@Serializable
data class DriverStanding(
    @SerialName("position") val position: String = "0",
    @SerialName("positionText") val positionText: String = "",
    @SerialName("points") val points: String = "0",
    @SerialName("wins") val wins: String = "0",
    @SerialName("Driver") val driver: Driver = Driver(),
    @SerialName("Constructors") val constructors: List<Constructor> = emptyList()
)

@Serializable
data class ConstructorStanding(
    @SerialName("position") val position: String = "0",
    @SerialName("positionText") val positionText: String = "",
    @SerialName("points") val points: String = "0",
    @SerialName("wins") val wins: String = "0",
    @SerialName("Constructor") val constructor: Constructor = Constructor()
)

@Serializable
data class FastestLap(
    @SerialName("rank") val rank: String = "",
    @SerialName("lap") val lap: String = "",
    @SerialName("Time") val time: LapTime? = null
)

@Serializable
data class LapTime(
    @SerialName("time") val time: String = ""
)

@Serializable
data class ResultTime(
    @SerialName("millis") val millis: String = "",
    @SerialName("time") val time: String = ""
)

@Serializable
data class RaceResult(
    @SerialName("number") val number: String = "",
    @SerialName("position") val position: String = "",
    @SerialName("positionText") val positionText: String = "",
    @SerialName("points") val points: String = "0",
    @SerialName("Driver") val driver: Driver = Driver(),
    @SerialName("Constructor") val constructor: Constructor = Constructor(),
    @SerialName("grid") val grid: String = "",
    @SerialName("laps") val laps: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("Time") val time: ResultTime? = null,
    @SerialName("FastestLap") val fastestLap: FastestLap? = null
)
