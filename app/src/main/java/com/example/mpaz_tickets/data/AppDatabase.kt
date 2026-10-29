package com.example.mpaz_tickets.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SolicitudReserva::class, UsuarioEntity::class, AuditoriaEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservasDao(): ReservasDao
    abstract fun usuariosDao(): UsuariosDao
    abstract fun auditoriaDao(): AuditoriaDao
}