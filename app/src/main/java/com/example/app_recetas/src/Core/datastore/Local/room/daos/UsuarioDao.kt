package com.example.app_recetas.src.datastore.Local.room.daos

import androidx.room.*
import com.example.app_recetas.src.datastore.Local.room.entities.UsuarioEntity

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario WHERE id = 1")
    suspend fun getUsuarioActual(): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity)

    @Query("DELETE FROM usuario")
    suspend fun deleteUsuario()

    @Query("SELECT token FROM usuario WHERE id = 1")
    suspend fun getToken(): String?
}