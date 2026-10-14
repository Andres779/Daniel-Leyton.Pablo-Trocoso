package com.example.mpaz_tickets.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservaViewModel : ViewModel() {

    // 1. ESTADOS Y FLUJOS (STATE FLOWS)
    private val _usuarios = MutableStateFlow<List<Usuario>>(
        listOf(
            Usuario("1", "Inspector General", "inspector@colegiomariapaz.cl", "123456", RolUsuario.INSPECTORIA),
            Usuario("2", "Profe Juan", "juan@colegiomariapaz.cl", "123456", RolUsuario.AUTORIZADO),
            Usuario("3", "Profe Ana", "ana@colegiomariapaz.cl", "123456", RolUsuario.AUTORIZADO)
        )
    )
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    val reservas: StateFlow<List<Reserva>> = _reservas.asStateFlow()

    private val _historial = MutableStateFlow<List<EventoAuditoria>>(emptyList())
    val historial: StateFlow<List<EventoAuditoria>> = _historial.asStateFlow()

    // Lista interna privada con todas las notificaciones y su destinatario objetivo
    private val _notificacionesInternas = MutableStateFlow<List<NotificacionConDestino>>(emptyList())

    // Flujo REACTIVO de notificaciones filtradas según el usuario logueado en tiempo real
    val notificaciones: StateFlow<List<Notificacion>> = combine(_usuarioActual, _notificacionesInternas) { usuario, listaNotis ->
        if (usuario == null) {
            emptyList()
        } else if (usuario.rol == RolUsuario.INSPECTORIA) {
            // Inspectoría ve notificaciones dirigidas a su ID o avisos globales de solicitudes
            listaNotis.filter { it.destinatarioId == usuario.id || it.esGlobalInspectoria }.map { it.notificacion }
        } else {
            // Personal Autorizado ve única y exclusivamente sus propias notificaciones
            listaNotis.filter { it.destinatarioId == usuario.id }.map { it.notificacion }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    // 2. GESTIÓN DE AUTENTICACIÓN Y SESIÓN
    fun iniciarSesion(correo: String, contrasenia: String): Boolean {
        val user = _usuarios.value.find {
            it.correo.trim().equals(correo.trim(), ignoreCase = true) && it.contrasenia == contrasenia
        }
        if (user != null) {
            _usuarioActual.value = user
            registrarAuditoria("Login", "Usuario ${user.nombre} inició sesión")
            return true
        }
        return false
    }

    fun cerrarSesion() {
        registrarAuditoria("Logout", "Usuario ${_usuarioActual.value?.nombre} cerró sesión")
        _usuarioActual.value = null
    }


    // 3. GESTIÓN DE USUARIOS (INSPECTORÍA)
    fun crearUsuario(nombre: String, correo: String, rol: RolUsuario, contrasenia: String): String {
        val correoLimpio = correo.trim().lowercase()

        if (!correoLimpio.endsWith("@colegiomariapaz.cl")) {
            return "El correo debe ser institucional (@colegiomariapaz.cl)"
        }
        if (_usuarios.value.any { it.correo.equals(correoLimpio, ignoreCase = true) }) {
            return "El correo ya está registrado"
        }

        val nuevoUser = Usuario(
            id = System.currentTimeMillis().toString(),
            nombre = nombre,
            correo = correoLimpio,
            contrasenia = contrasenia,
            rol = rol
        )

        _usuarios.value = _usuarios.value + nuevoUser
        registrarAuditoria("Usuario Creado", "Inspectoría creó a $nombre (${rol.displayName})")
        return "Éxito"
    }

    fun eliminarUsuario(idUsuario: String) {
        val usuarioAEliminar = _usuarios.value.find { it.id == idUsuario }
        if (usuarioAEliminar?.rol == RolUsuario.INSPECTORIA && _usuarios.value.count { it.rol == RolUsuario.INSPECTORIA } <= 1) {
            return
        }
        _usuarios.value = _usuarios.value.filter { it.id != idUsuario }
        registrarAuditoria("Usuario Eliminado", "Se eliminó al usuario: ${usuarioAEliminar?.nombre ?: idUsuario}")
    }


    // 4. GESTIÓN DE RESERVAS Y BLOQUEOS
    fun obtenerReservasFiltradas(): List<Reserva> {
        val usuario = _usuarioActual.value
        val listaReservas = _reservas.value
        return if (usuario?.rol == RolUsuario.AUTORIZADO) {
            listaReservas.filter { it.solicitanteId == usuario.id }
        } else {
            listaReservas
        }
    }

    fun obtenerReservasPersonales(): List<Reserva> {
        val usuario = _usuarioActual.value
        val listaReservas = _reservas.value ?: emptyList()
        return if (usuario != null) {
            listaReservas.filter { it.solicitanteId == usuario.id }
        } else {
            emptyList()
        }
    }

    fun crearReserva(
        espacio: TipoEspacio,
        motivo: String,
        fechaReserva: String,
        horaInicio: String,
        horaFin: String,
        actividadEspecifica: String,
        cursoGrupo: String,
        cantidadParticipantesStr: String
    ): String {
        val cantidad = cantidadParticipantesStr.toIntOrNull() ?: 0

        // 1. Validación de Capacidad Máxima del Espacio
        if (cantidad > espacio.capacidadMaxima) {
            return "La cantidad de participantes ($cantidad) supera la capacidad máxima de '${espacio.displayName}' (${espacio.capacidadMaxima} personas)."
        }

        val usuario = _usuarioActual.value
        val nombre = usuario?.nombre ?: "Desconocido"
        val idSolicitante = usuario?.id ?: ""

        // 2. Validación de reservas duplicadas
        val yaExisteDuplicado = _reservas.value.any {
            it.espacio == espacio &&
                    it.fechaReserva == fechaReserva &&
                    it.horaInicio == horaInicio &&
                    it.horaFin == horaFin &&
                    (it.estado == EstadoReserva.PENDIENTE || it.estado == EstadoReserva.APROBADA)
        }

        if (yaExisteDuplicado) {
            return "El espacio '${espacio.displayName}' ya se encuentra reservado u ocupado en ese horario."
        }

        val fechaSolicitud = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val nuevaReserva = Reserva(
            id = "RES-${(1000..9999).random()}",
            solicitanteId = idSolicitante,
            solicitanteNombre = nombre,
            espacio = espacio,
            motivo = motivo,
            fechaReserva = fechaReserva,
            horaInicio = horaInicio,
            horaFin = horaFin,
            estado = EstadoReserva.PENDIENTE,
            fechaSolicitud = fechaSolicitud,
            actividadEspecifica = actividadEspecifica,
            cursoGrupo = cursoGrupo,
            cantidadParticipantes = cantidad
        )

        _reservas.value = listOf(nuevaReserva) + _reservas.value
        registrarAuditoria("Reserva Creada", "Reserva ${nuevaReserva.id} por $nombre para ${espacio.displayName} ($cantidad pers.)")

        // Notificar a Inspectoría
        val inspectores = _usuarios.value.filter { it.rol == RolUsuario.INSPECTORIA }
        for (inspector in inspectores) {
            agregarNotificacion(
                destinatarioId = inspector.id,
                titulo = "Nueva Solicitud Pendiente",
                mensaje = "$nombre solicitó '${espacio.displayName}' para el $fechaReserva ($cantidad part.).",
                esGlobal = true
            )
        }

        return "Éxito"
    }

    fun actualizarEstado(reservaId: String, nuevoEstado: EstadoReserva, justificacion: String = "") {
        val reservaAfectada = _reservas.value.find { it.id == reservaId }

        _reservas.value = _reservas.value.map {
            if (it.id == reservaId) it.copy(estado = nuevoEstado) else it
        }

        val detalleAuditoria = if (justificacion.isNotBlank()) {
            "Estado de $reservaId cambió a ${nuevoEstado.displayName}. Motivo: $justificacion"
        } else {
            "Estado de $reservaId cambió a ${nuevoEstado.displayName}"
        }
        registrarAuditoria("Actualización", detalleAuditoria)

        // Enviar la notificación de estado ÚNICAMENTE al dueño/solicitante de la reserva
        if (reservaAfectada != null) {
            val titulo = when (nuevoEstado) {
                EstadoReserva.APROBADA -> "¡Reserva Aprobada!"
                EstadoReserva.RECHAZADA -> "Reserva Rechazada"
                EstadoReserva.CANCELADA -> "Reserva Cancelada"
                else -> "Actualización de Reserva"
            }

            var mensaje = "Tu solicitud para '${reservaAfectada.espacio.displayName}' el ${reservaAfectada.fechaReserva} ha sido ${nuevoEstado.displayName.lowercase()}."
            if (justificacion.isNotBlank()) {
                mensaje += " Motivo: $justificacion"
            }

            agregarNotificacion(
                destinatarioId = reservaAfectada.solicitanteId,
                titulo = titulo,
                mensaje = mensaje,
                esGlobal = false
            )
        }
    }

    fun bloquearEspacio(espacio: TipoEspacio, fecha: String, horaInicio: String, horaFin: String, motivo: String) {
        val usuario = _usuarioActual.value
        if (usuario?.rol != RolUsuario.INSPECTORIA) return

        val fechaSolicitud = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val bloqueoReserva = Reserva(
            id = "BLOQUEO-${(1000..9999).random()}",
            solicitanteId = usuario.id,
            solicitanteNombre = "[BLOQUEO INSTITUCIONAL] ${usuario.nombre}",
            espacio = espacio,
            motivo = "BLOQUEO: $motivo",
            fechaReserva = fecha,
            horaInicio = horaInicio,
            horaFin = horaFin,
            estado = EstadoReserva.RECHAZADA,
            fechaSolicitud = fechaSolicitud
        )

        _reservas.value = listOf(bloqueoReserva) + _reservas.value
        registrarAuditoria("Bloqueo de Espacio", "Se bloqueó '${espacio.displayName}' para el $fecha ($horaInicio - $horaFin). Motivo: $motivo")
    }


    // 5. SISTEMA DE NOTIFICACIONES IN-APP (Filtrado Reactivo por Usuario)
    private fun agregarNotificacion(destinatarioId: String, titulo: String, mensaje: String, esGlobal: Boolean) {
        val fechaHoraActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val nuevaNoti = Notificacion(
            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
            titulo = titulo,
            mensaje = mensaje,
            fechaHora = fechaHoraActual,
            leida = false
        )
        val itemConDestino = NotificacionConDestino(
            destinatarioId = destinatarioId,
            esGlobalInspectoria = esGlobal,
            notificacion = nuevaNoti
        )
        _notificacionesInternas.value = listOf(itemConDestino) + _notificacionesInternas.value
    }

    fun marcarNotificacionesComoLeidas() {
        val usuario = _usuarioActual.value ?: return
        _notificacionesInternas.value = _notificacionesInternas.value.map { item ->
            val esParaUsuario = item.destinatarioId == usuario.id || (usuario.rol == RolUsuario.INSPECTORIA && item.esGlobalInspectoria)
            if (esParaUsuario) {
                item.copy(notificacion = item.notificacion.copy(leida = true))
            } else {
                item
            }
        }
    }


    // 6. AUDITORÍA Y REGISTROS
    private fun registrarAuditoria(accion: String, detalle: String) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val evento = EventoAuditoria(System.currentTimeMillis().toString(), fecha, _usuarioActual.value?.nombre ?: "Sistema", accion, detalle)
        _historial.value = listOf(evento) + _historial.value
    }

    fun limpiarHistorial() {
        _historial.value = emptyList()
    }

    fun registrarCambioEstadoEspacio(espacio: String, nuevoEstado: String) {
        registrarAuditoria(
            accion = "Cambio de Estado de Espacio",
            detalle = "El recinto '$espacio' cambió a estado: '$nuevoEstado'"
        )
    }

    // 7. GESTIÓN DE ESPACIOS Y ESTADOS
    private val _espaciosDetalle = MutableStateFlow<List<EspacioDetalle>>(
        listOf(
            EspacioDetalle(TipoEspacio.LABORATORIO, "Laboratorio de Computación equipado con 30 PCs, proyector y pizarra inteligente.", "Pabellón A, Piso 2", 30, "Disponible"),
            EspacioDetalle(TipoEspacio.LABORATORIO_CIENCIAS, "Laboratorio de Ciencias equipado para experiencias científicas y experimentación escolar.", "Pabellón B, Piso 2", 25, "Disponible"),
            EspacioDetalle(TipoEspacio.BIBLIOTECA, "Espacio de lectura silenciosa con mesas de trabajo grupal y colecciones bibliográficas.", "Pabellón B, Piso 1", 40, "Disponible"),
            EspacioDetalle(TipoEspacio.SALA_SENSORIAL, "Sala acondicionada con estímulos controlados para apoyo pedagógico y bienestar.", "Pabellón C, Piso 1", 15, "Disponible"),
            EspacioDetalle(TipoEspacio.CIEMP, "Centro de Innovación Educativa Marcela Paz con tecnología y herramientas digitales avanzadas.", "Edificio Central, Piso 2", 20, "Disponible"),
            EspacioDetalle(TipoEspacio.SALA_REUNIONES, "Sala insonorizada ideal para reuniones de apoderados, UTP o consejo de profesores.", "Edificio Administrativo, Piso 1", 12, "Disponible"),
            EspacioDetalle(TipoEspacio.HUERTO, "Huerto escolar al aire libre para actividades de ciencias naturales y educación ambiental.", "Zona Exterior / Patio Trasterio", 35, "Disponible")
        )
    )
    val espaciosDetalle: StateFlow<List<EspacioDetalle>> = _espaciosDetalle.asStateFlow()

    fun actualizarEstadoEspacio(tipo: TipoEspacio, nuevoEstado: String) {
        _espaciosDetalle.value = _espaciosDetalle.value.map { item ->
            if (item.tipo == tipo) item.copy(estado = nuevoEstado) else item
        }
        registrarCambioEstadoEspacio(tipo.displayName, nuevoEstado)
    }
}

// Estructura auxiliar para vincular destinatarios
data class NotificacionConDestino(
    val destinatarioId: String,
    val esGlobalInspectoria: Boolean,
    val notificacion: Notificacion
)