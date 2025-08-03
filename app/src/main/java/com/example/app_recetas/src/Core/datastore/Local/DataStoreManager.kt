package com.example.app_recetas.src.Core.datastore.Local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

class DataStoreManager(private val context: Context) {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recetas_settings")

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    suspend fun saveKey(key: Preferences.Key<String>, value: String) {

        context.dataStore.edit { prefs ->
            prefs[key] = value
        }

        val saved = context.dataStore.data.first()[key]
        Log.d("DataStoreManager", "🔍 Verificación inmediata: $saved")
    }

    suspend fun getKey(key: Preferences.Key<String>): String? {
        Log.d("DataStoreManager", "📖 Leyendo de DataStore...")
        Log.d("DataStoreManager", "🔑 Key: ${key.name}")

        val value = context.dataStore.data.first()[key]
        Log.d("DataStoreManager", "💰 Value encontrado: $value")
        return value
    }

    suspend fun removeKey(key: Preferences.Key<*>) {
        Log.d("DataStoreManager", "🗑️ Eliminando key: ${key.name}")
        context.dataStore.edit { prefs -> prefs.remove(key) }
    }

    suspend fun saveToken(token: String) {
        Log.d("DataStoreManager", "🎫 Guardando token específicamente...")
        saveKey(TOKEN_KEY, token)
    }

    suspend fun getToken(): String? {
        Log.d("DataStoreManager", "🎫 Obteniendo token específicamente...")
        return getKey(TOKEN_KEY)
    }
}