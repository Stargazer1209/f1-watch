package com.f1watch.data.local

import android.content.Context
import com.f1watch.data.model.LocalCalendarResponse
import com.f1watch.data.model.Race
import kotlinx.serialization.json.Json

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
}
