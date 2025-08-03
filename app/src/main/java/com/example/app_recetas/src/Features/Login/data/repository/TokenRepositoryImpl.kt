package com.example.app_recetas.src.Features.Login.data.repository

import com.example.app_recetas.src.Core.datastore.Local.DataStoreManager
import com.example.app_recetas.src.Core.datastore.Local.PreferenceKeys
import com.example.app_recetas.src.Features.Login.domain.repository.TokenRepository

class TokenRepositoryImpl(
    private val dataStore: DataStoreManager
) : TokenRepository {

    override suspend fun getToken(): String? = dataStore.getKey(PreferenceKeys.TOKEN)

    override suspend fun saveToken(token: String) = dataStore.saveKey(PreferenceKeys.TOKEN, token)

    override suspend fun clearToken() = dataStore.removeKey(PreferenceKeys.TOKEN)
}