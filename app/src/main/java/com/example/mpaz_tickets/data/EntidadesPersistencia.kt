package com.example.mpaz_tickets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val correo: String,
    val contrasenia: String,
    val rol: String
)

@Entity(tableName = "auditoria")
data class AuditoriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fechaHora: String,
    val usuario: String,
    val accion: String,
    val detalle: String
)