package com.example.mpaz_tickets.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SolicitudReserva::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reservasDao(): ReservasDao
}