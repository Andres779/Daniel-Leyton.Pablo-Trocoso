package com.example.mpaz_tickets.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ReservasDao {
    @Insert
    suspend fun insertar(solicitud: SolicitudReserva)

    @Query("SELECT * FROM solicitudes")
    suspend fun listar(): List<SolicitudReserva>

    @Update
    suspend fun actualizar(solicitud: SolicitudReserva)
}