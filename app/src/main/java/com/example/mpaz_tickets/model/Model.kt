package com.example.mpaz_tickets.model

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val contrasenia: String,
    val rol: RolUsuario
)

enum class RolUsuario(val displayName: String) {
    AUTORIZADO("Personal Autorizado"), // docentes, UTP, Dirección y profesionales PIE o de apoyo
    INSPECTORIA("Inspectoría")
}

enum class TipoEspacio(val displayName: String, val capacidadMaxima: Int) {
    LABORATORIO("Laboratorio de Computación", 30),
    LABORATORIO_CIENCIAS("Laboratorio de Ciencias", 25),
    BIBLIOTECA("Biblioteca", 40),
    SALA_SENSORIAL("Sala Sensorial", 15),
    CIEMP("Centro de Innovación CIEMP", 20),
    SALA_REUNIONES("Sala de Reuniones / Capacitación", 12),
    HUERTO("Huerto Escolar", 35)
}

enum class EstadoReserva(val displayName: String) {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada")
}

data class Reserva(
    val id: String,
    val solicitanteId: String,
    val solicitanteNombre: String,
    val espacio: TipoEspacio,
    val motivo: String,
    val fechaReserva: String,
    val horaInicio: String,
    val horaFin: String,
    val estado: EstadoReserva,
    val fechaSolicitud: String,
    val actividadEspecifica: String = "",
    val cursoGrupo: String = "",
    val cantidadParticipantes: Int = 0
)

data class EventoAuditoria(
    val id: String,
    val fechaHora: String,
    val usuario: String,
    val accion: String,
    val detalle: String
)

data class EspacioDetalle(
    val tipo: TipoEspacio,
    val descripcion: String,
    val ubicacion: String,
    val capacidad: Int,
    val estado: String
)

data class Notificacion(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val fechaHora: String,
    val leida: Boolean = false
)