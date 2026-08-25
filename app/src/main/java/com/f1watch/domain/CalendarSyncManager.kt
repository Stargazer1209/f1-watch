package com.f1watch.domain

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.f1watch.data.model.Race
import com.f1watch.data.model.SessionItem
import java.util.TimeZone

object CalendarSyncManager {

    /**
     * Launch System Calendar insert intent for a single session.
     * This method does not require direct WRITE_CALENDAR runtime permission,
     * as it delegates user confirmation to the system Calendar app.
     */
    fun openCalendarInsertIntent(context: Context, race: Race, session: SessionItem) {
        val zdt = session.sessionTime.toZonedDateTime() ?: return
        val startMillis = zdt.toInstant().toEpochMilli()
        val durationMillis = if (session.shortName == "RACE") 2 * 3600 * 1000L else 1 * 3600 * 1000L
        val endMillis = startMillis + durationMillis

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "F1 2026: ${race.raceName} - ${session.name}")
            putExtra(CalendarContract.Events.EVENT_LOCATION, "${race.circuit.circuitName}, ${race.circuit.locality}")
            putExtra(CalendarContract.Events.DESCRIPTION, "F1 2026 赛季第 ${race.round} 站：${race.raceName}\n阶段：${session.name}\n赛道：${race.circuit.circuitName}")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Directly inserts all sessions of the given race into the primary system calendar
     * when WRITE_CALENDAR permission is granted.
     */
    fun syncAllSessionsDirectly(context: Context, race: Race, reminderMinutes: Int = 15): Result<Int> {
        return try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY
            )
            val uri = CalendarContract.Calendars.CONTENT_URI
            val cursor = context.contentResolver.query(
                uri,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
            )

            var calendarId: Long = 1
            cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(CalendarContract.Calendars._ID)
                    calendarId = it.getLong(idIndex)
                }
            }

            var insertedCount = 0
            val sessions = race.getAllSessions()

            for (session in sessions) {
                val zdt = session.sessionTime.toZonedDateTime() ?: continue
                val startMillis = zdt.toInstant().toEpochMilli()
                val durationMillis = if (session.shortName == "RACE") 2 * 3600 * 1000L else 1 * 3600 * 1000L
                val endMillis = startMillis + durationMillis

                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.TITLE, "🏎️ F1: ${race.raceName} - ${session.shortName}")
                    put(CalendarContract.Events.DESCRIPTION, "F1 2026 Round ${race.round}: ${race.raceName}\n${session.name}\n赛道: ${race.circuit.circuitName}")
                    put(CalendarContract.Events.EVENT_LOCATION, "${race.circuit.circuitName}, ${race.circuit.country}")
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }

                val eventUri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                eventUri?.let { insertedEventUri ->
                    insertedCount++
                    val eventId = insertedEventUri.lastPathSegment?.toLongOrNull()
                    if (eventId != null && reminderMinutes > 0) {
                        val reminderValues = ContentValues().apply {
                            put(CalendarContract.Reminders.MINUTES, reminderMinutes)
                            put(CalendarContract.Reminders.EVENT_ID, eventId)
                            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                        }
                        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                    }
                }
            }

            Result.success(insertedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
