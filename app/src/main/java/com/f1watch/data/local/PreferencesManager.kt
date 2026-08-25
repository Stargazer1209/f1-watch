package com.f1watch.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "f1_watch_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("key_widget_theme")
        val KEY_OPACITY = floatPreferencesKey("key_widget_opacity")
        val KEY_REMINDER_MINUTES = stringPreferencesKey("key_reminder_minutes")
        val KEY_LAST_SYNC = longPreferencesKey("key_last_sync_time")
        val KEY_SHOW_PRACTICE = booleanPreferencesKey("key_show_practice_in_widget")
    }

    val widgetThemeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "f1_red"
    }

    val widgetOpacityFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPACITY] ?: 0.90f
    }

    val showPracticeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SHOW_PRACTICE] ?: true
    }

    suspend fun setWidgetTheme(themeKey: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = themeKey
        }
    }

    suspend fun setWidgetOpacity(opacity: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPACITY] = opacity
        }
    }

    suspend fun setShowPractice(show: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_PRACTICE] = show
        }
    }

    suspend fun updateLastSyncTime() {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNC] = System.currentTimeMillis()
        }
    }
}
