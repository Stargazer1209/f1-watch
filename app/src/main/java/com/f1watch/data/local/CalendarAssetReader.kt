package com.f1watch.data.local

import android.content.Context
import com.f1watch.data.model.ConstructorStanding
import com.f1watch.data.model.DriverStanding
import com.f1watch.data.model.LocalCalendarResponse
import com.f1watch.data.model.Race
import com.f1watch.data.model.RaceResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

object CalendarAssetReader {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    fun loadDefaultCalendar(context: Context): List<Race> {
        return try {
            val jsonString = context.assets.open("2026_calendar.json").bufferedReader().use { it.readText() }
            val response = json.decodeFromString<LocalCalendarResponse>(jsonString)
            response.races
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadDefaultDriverStandings(context: Context): List<DriverStanding> {
        return try {
            val jsonString = context.assets.open("2026_driver_standings.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<DriverStanding>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadDefaultConstructorStandings(context: Context): List<ConstructorStanding> {
        return try {
            val jsonString = context.assets.open("2026_constructor_standings.json").bufferedReader().use { it.readText() }
            json.decodeFromString<List<ConstructorStanding>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadDefaultRaceResults(context: Context, round: String): List<RaceResult> {
        return try {
            val jsonString = context.assets.open("2026_race_results.json").bufferedReader().use { it.readText() }
            val element = json.parseToJsonElement(jsonString).jsonObject
            val roundElement = element[round] ?: return emptyList()
            json.decodeFromJsonElement<List<RaceResult>>(roundElement)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
