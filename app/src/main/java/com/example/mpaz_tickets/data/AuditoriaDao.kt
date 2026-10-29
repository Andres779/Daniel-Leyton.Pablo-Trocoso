package com.example.mpaz_tickets.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface AuditoriaDao {
    @Query("SELECT * FROM auditoria ORDER BY id DESC")
    fun listar(): Flow<List<AuditoriaEntity>>

    @Insert
    suspend fun insertar(evento: AuditoriaEntity): Long

    @Query("DELETE FROM auditoria")
    suspend fun limpiar(): Int
}