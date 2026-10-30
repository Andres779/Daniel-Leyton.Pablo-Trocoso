package com.example.mpaz_tickets.data

import kotlinx.coroutines.flow.Flow

class ReservasRepository(private val dao: ReservasDao,
                         private val usuariosDao: UsuariosDao,
                         private val auditoriaDao: AuditoriaDao
) {

    fun obtenerUsuarios(): Flow<List<UsuarioEntity>> = usuariosDao.listar()

    suspend fun sembrarUsuariosSiVacio(iniciales: List<UsuarioEntity>) {
        if (usuariosDao.contar() == 0) usuariosDao.insertarTodos(iniciales)
    }

    suspend fun guardarUsuario(usuario: UsuarioEntity) = usuariosDao.insertar(usuario)

    suspend fun eliminarUsuario(id: String) = usuariosDao.eliminar(id)

    // ---------- AUDITORÍA ----------
    fun obtenerHistorial(): Flow<List<AuditoriaEntity>> = auditoriaDao.listar()

    suspend fun registrarEvento(evento: AuditoriaEntity) = auditoriaDao.insertar(evento)

    suspend fun limpiarHistorial() = auditoriaDao.limpiar()

    val catalogoEspacios = listOf(
            Espacio("1", "Biblioteca", "Lectura y Estudio", 30),
            Espacio("2", "Laboratorio de Computación", "Tecnología", 40),
            Espacio("3", "Huerto Escolar", "Ciencias Naturales", 15),
            Espacio("4", "Sala Sensorial", "Apoyo PIE", 10)
    )

    fun obtenerTodasLasReservas(): Flow<List<SolicitudReserva>> {
        return dao.listar()
    }

    suspend fun crearSolicitud(nuevaSolicitud: SolicitudReserva): Result<String> {
        val tiempoActual = System.currentTimeMillis()
        val unDiaEnMilisegundos = 24L * 60 * 60 * 1000L

        if (nuevaSolicitud.fechaHoraInicio - tiempoActual < unDiaEnMilisegundos) {
            return Result.failure(Exception("Error: Debe solicitar con al menos 24 horas de anticipación."))
        }

        // Se inserta físicamente en la base de datos local
        dao.insertar(nuevaSolicitud)
        return Result.success("Solicitud enviada exitosamente.")
    }

    suspend fun responderSolicitud(
        solicitud: SolicitudReserva,
        nuevoEstado: String,
        justificacion: String?
    ): Result<String> {
        if (nuevoEstado == "RECHAZADA" && justificacion.isNullOrBlank()) {
            return Result.failure(Exception("Error: Es obligatorio ingresar una justificación para rechazar."))
        }

        solicitud.estado = nuevoEstado
        solicitud.justificacionRechazo = justificacion

        dao.actualizar(solicitud)
        return Result.success("Estado actualizado a $nuevoEstado.")
    }
}