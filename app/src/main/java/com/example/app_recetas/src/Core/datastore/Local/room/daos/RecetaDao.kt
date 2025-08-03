package com.example.app_recetas.src.datastore.Local.room.daos

import androidx.room.*
import com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecetaDao {

    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    fun getAllRecetas(): Flow<List<RecetaEntity>>

    @Query("SELECT * FROM recetas WHERE id = :id")
    suspend fun getRecetaById(id: Int): RecetaEntity?

    @Query("SELECT * FROM recetas WHERE sincronizado = 0")
    suspend fun getRecetasNoSincronizadas(): List<RecetaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceta(receta: RecetaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecetas(recetas: List<RecetaEntity>)

    @Update
    suspend fun updateReceta(receta: RecetaEntity)

    @Delete
    suspend fun deleteReceta(receta: RecetaEntity)

    @Query("DELETE FROM recetas WHERE id = :id")
    suspend fun deleteRecetaById(id: Int)

    @Query("UPDATE recetas SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarComoSincronizado(id: Int)

    @Query("DELETE FROM recetas")
    suspend fun deleteAllRecetas()
}