package com.example.app_recetas.src.Core.Http.Interceptor

import android.util.Log
import com.example.app_recetas.src.Core.datastore.Local.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AddTokenInterceptor(
    private val dataStore: DataStoreManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("AddTokenInterceptor", "🔄 Interceptando petición...")
        Log.d("AddTokenInterceptor", "🌐 URL: ${chain.request().url}")

        val requestBuilder = chain.request().newBuilder()

        val token = runBlocking {
            try {
                Log.d("AddTokenInterceptor", "📖 Intentando leer token del DataStore...")
                val retrievedToken = dataStore.getToken()
                Log.d("AddTokenInterceptor", "🎫 Token obtenido: $retrievedToken")
                retrievedToken
            } catch (e: Exception) {
                Log.e("AddTokenInterceptor", "❌ Error obteniendo token: ${e.message}", e)
                null
            }
        }

        token?.let {
            Log.d("AddTokenInterceptor", "✅ Agregando token a la petición: Bearer $it")
            requestBuilder.addHeader("Authorization", "Bearer $it")
        } ?: run {
            Log.w("AddTokenInterceptor", "⚠️ No se encontró token, enviando petición sin Authorization")
        }

        val request = requestBuilder.build()
        Log.d("AddTokenInterceptor", "📋 Headers de la petición:")
        request.headers.forEach { header ->
            Log.d("AddTokenInterceptor", "  ${header.first}: ${header.second}")
        }

        val response = chain.proceed(request)
        Log.d("AddTokenInterceptor", "📥 Respuesta recibida - Código: ${response.code}")

        return response
    }
}