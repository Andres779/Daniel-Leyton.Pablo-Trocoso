package com.example.mpaz_tickets.data;

import kotlin.Result;

class ReservasRepository {

    // Datos mock para que el Frontend muestre el catálogo desde hoy
    val catalogoEspacios = listOf(
            Espacio("1", "Biblioteca", "Lectura y Estudio", 30),
            Espacio("2", "Laboratorio de Computación", "Tecnología", 40),
            Espacio("3", "Huerto Escolar", "Ciencias Naturales", 15),
            Espacio("4", "Sala Sensorial", "Apoyo PIE", 10)
    )

    private val listaSolicitudes = mutableListOf<SolicitudReserva>()

    // Función que llamará el Frontend al presionar "Enviar Reserva"
    fun crearSolicitud(nuevaSolicitud: SolicitudReserva):Result<String>

    {
        val tiempoActual = System.currentTimeMillis()
        val unDiaEnMilisegundos = 24 * 60 * 60 * 1000L

        // Regla 1: Validación de 1 día hábil de anticipación
        if (nuevaSolicitud.fechaHoraInicio - tiempoActual < unDiaEnMilisegundos) {
            return Result.failure(Exception("Error: Debe solicitar con al menos 24 horas de anticipación."))
        }

        // Regla 2: Prevención de solapamiento/duplicados
        val existeSolapamiento = listaSolicitudes.any { reserva ->
                reserva.espacioId == nuevaSolicitud.espacioId &&
                        reserva.estado == EstadoSolicitud.ACEPTADA &&
                        nuevaSolicitud.fechaHoraInicio < reserva.fechaHoraFin &&
                        nuevaSolicitud.fechaHoraFin > reserva.fechaHoraInicio
        }

        if (existeSolapamiento) {
            return Result.failure(Exception("Error: El espacio ya se encuentra reservado en ese horario."))
        }

        listaSolicitudes.add(nuevaSolicitud)
        return Result.success("Solicitud enviada exitosamente.")
    }

    // Función que llamará Inspectoría para evaluar reservas
    fun responderSolicitud(id: String, nuevoEstado: EstadoSolicitud, justificacion: String?): Result<String> {
        // Regla 3: Justificación obligatoria al rechazar
        if (nuevoEstado == EstadoSolicitud.RECHAZADA && justificacion.isNullOrBlank()) {
            return Result.failure(Exception("Error: Es obligatorio ingresar una justificación para rechazar."))
        }

        val solicitud = listaSolicitudes.find { it.id == id }
            ?: return Result.failure(Exception("Solicitud no encontrada."))

        solicitud.estado = nuevoEstado
        solicitud.justificacionRechazo = justificacion
        return Result.success("Estado actualizado a $nuevoEstado.")
    }
}