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
    @PrimaryKey val id: String,
    val solicitanteId: String,
    val solicitanteNombre: String,
    val espacio: String,
    val motivo: String,
    val fechaReserva: String,
    val horaInicio: String,
    val horaFin: String,
    var estado: String,
    val fechaSolicitud: String,
    val actividadEspecifica: String,
    val cursoGrupo: String,
    val cantidadParticipantes: Int,
    var fechaHoraInicio: Long = 0L,
    var justificacionRechazo: String? = null
)