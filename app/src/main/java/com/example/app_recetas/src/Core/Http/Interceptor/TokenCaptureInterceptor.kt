package com.example.app_recetas.src.Core.Http.Interceptor

import android.util.Log
import com.example.app_recetas.src.Core.datastore.Local.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

class TokenCaptureInterceptor(
    private val dataStore: DataStoreManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val authHeader = response.header("Authorization")
        if (!authHeader.isNullOrEmpty()) {
            Log.d("TokenCaptureInterceptor", "🎫 Capturando token de respuesta: $authHeader")
            CoroutineScope(Dispatchers.IO).launch {
                dataStore.saveToken(authHeader)
                Log.d("TokenCaptureInterceptor", "✅ Token capturado guardado")
            }
        } else {
            Log.d("TokenCaptureInterceptor", "ℹ️ No se encontró header Authorization en respuesta")
        }

        return response
    }
}