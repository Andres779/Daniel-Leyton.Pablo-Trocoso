package com.example.mpaz_tickets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoEspacio { DISPONIBLE, RESERVADO, BLOQUEADO, MANTENIMIENTO }
enum class EstadoSolicitud { PENDIENTE, ACEPTADA, RECHAZADA, CANCELADA }
enum class RolUsuario { SOLICITANTE, ADMINISTRADOR }

data class Espacio(
    val id: String,
    val nombre: String,
    val tipo: String,
    val capacidadMaxima: Int,
    val estado: EstadoEspacio = EstadoEspacio.DISPONIBLE
)

@Entity(tableName = "solicitudes")
data class SolicitudReserva(
    @PrimaryKey
    val id: String,
    val espacioId: String,
    val fechaHoraInicio: Long,
    val fechaHoraFin: Long,
    val actividad: String,
    var justificacionRechazo: String? = null,
    var estado: String = EstadoSolicitud.PENDIENTE.name
)