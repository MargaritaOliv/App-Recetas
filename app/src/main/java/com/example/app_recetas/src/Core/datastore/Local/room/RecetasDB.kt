package com.example.app_recetas.src.datastore.Local.room
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.app_recetas.src.datastore.Local.room.entities.RecetaEntity
import com.example.app_recetas.src.datastore.Local.room.entities.UsuarioEntity
import com.example.app_recetas.src.datastore.Local.room.entities.PendienteSincronizacionEntity
import com.example.app_recetas.src.datastore.Local.room.daos.RecetaDao
import com.example.app_recetas.src.datastore.Local.room.daos.UsuarioDao
import com.example.app_recetas.src.datastore.Local.room.daos.PendienteSincronizacionDao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

@Database(
    entities = [
        RecetaEntity::class,
        UsuarioEntity::class,
        PendienteSincronizacionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class RecetasDB : RoomDatabase() {

    abstract fun recetaDao(): RecetaDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun pendienteSincronizacionDao(): PendienteSincronizacionDao

    companion object {
        @Volatile
        private var INSTANCE: RecetasDB? = null

        fun getDatabase(context: Context): RecetasDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecetasDB::class.java,
                    "recetas_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}