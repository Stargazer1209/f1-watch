package com.f1watch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalCalendarResponse(
    @SerialName("season") val season: String = "2026",
    @SerialName("races") val races: List<Race> = emptyList()
)

@Serializable
data class JolpicaRootResponse<T>(
    @SerialName("MRData") val mrData: T
)

@Serializable
data class RaceTableResponse(
    @SerialName("RaceTable") val raceTable: RaceTable = RaceTable()
)

@Serializable
data class RaceTable(
    @SerialName("season") val season: String = "",
    @SerialName("round") val round: String = "",
    @SerialName("Races") val races: List<JolpicaRaceDto> = emptyList()
)

@Serializable
data class JolpicaRaceDto(
    @SerialName("season") val season: String = "",
    @SerialName("round") val round: String = "",
    @SerialName("raceName") val raceName: String = "",
    @SerialName("Circuit") val circuit: CircuitDto = CircuitDto(),
    @SerialName("date") val date: String = "",
    @SerialName("time") val time: String = "",
    @SerialName("FirstPractice") val firstPractice: SessionTime? = null,
    @SerialName("SecondPractice") val secondPractice: SessionTime? = null,
    @SerialName("ThirdPractice") val thirdPractice: SessionTime? = null,
    @SerialName("Qualifying") val qualifying: SessionTime? = null,
    @SerialName("SprintQualifying") val sprintQualifying: SessionTime? = null,
    @SerialName("Sprint") val sprint: SessionTime? = null,
    @SerialName("Results") val results: List<RaceResult> = emptyList()
) {
    fun toDomainRace(countryCodeMap: Map<String, String>): Race {
        val code = countryCodeMap[circuit.location.country] ?: ""
        return Race(
            round = round,
            raceName = raceName,
            circuit = Circuit(
                circuitId = circuit.circuitId,
                circuitName = circuit.circuitName,
                locality = circuit.location.locality,
                country = circuit.location.country,
                countryCode = code,
                flagEmoji = getFlagEmojiForCountry(circuit.location.country),
                lat = circuit.location.lat.toDoubleOrNull() ?: 0.0,
                long = circuit.location.long.toDoubleOrNull() ?: 0.0
            ),
            date = date,
            time = time,
            firstPractice = firstPractice,
            secondPractice = secondPractice,
            thirdPractice = thirdPractice,
            qualifying = qualifying,
            sprintQualifying = sprintQualifying,
            sprint = sprint
        )
    }

    private fun getFlagEmojiForCountry(country: String): String {
        return when (country.lowercase()) {
            "australia" -> "🇦🇺"
            "china" -> "🇨🇳"
            "japan" -> "🇯🇵"
            "bahrain" -> "🇧🇭"
            "saudi arabia" -> "🇸🇦"
            "usa", "united states" -> "🇺🇸"
            "italy" -> "🇮🇹"
            "monaco" -> "🇲🇨"
            "spain" -> "🇪🇸"
            "canada" -> "🇨🇦"
            "austria" -> "🇦🇹"
            "uk", "great britain", "united kingdom" -> "🇬🇧"
            "belgium" -> "🇧🇪"
            "hungary" -> "🇭🇺"
            "netherlands" -> "🇳🇱"
            "azerbaijan" -> "🇦🇿"
            "singapore" -> "🇸🇬"
            "mexico" -> "🇲🇽"
            "brazil" -> "🇧🇷"
            "qatar" -> "🇶🇦"
            "uae", "united arab emirates" -> "🇦🇪"
            else -> "🏁"
        }
    }
}

@Serializable
data class CircuitDto(
    @SerialName("circuitId") val circuitId: String = "",
    @SerialName("circuitName") val circuitName: String = "",
    @SerialName("Location") val location: LocationDto = LocationDto()
)

@Serializable
data class LocationDto(
    @SerialName("lat") val lat: String = "0",
    @SerialName("long") val long: String = "0",
    @SerialName("locality") val locality: String = "",
    @SerialName("country") val country: String = ""
)

@Serializable
data class StandingsTableResponse(
    @SerialName("StandingsTable") val standingsTable: StandingsTable = StandingsTable()
)

@Serializable
data class StandingsTable(
    @SerialName("season") val season: String = "",
    @SerialName("StandingsLists") val standingsLists: List<StandingsList> = emptyList()
)

@Serializable
data class StandingsList(
    @SerialName("season") val season: String = "",
    @SerialName("round") val round: String = "",
    @SerialName("DriverStandings") val driverStandings: List<DriverStanding> = emptyList(),
    @SerialName("ConstructorStandings") val constructorStandings: List<ConstructorStanding> = emptyList()
)
