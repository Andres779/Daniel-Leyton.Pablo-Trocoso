package com.example.mpaz_tickets.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.EstadoReserva
import com.example.mpaz_tickets.model.RolUsuario
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun AdminDashboardScreen(viewModel: ReservaViewModel) {
    val reservas by viewModel.reservas.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()

    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var pestañaSeleccionada by remember { mutableStateOf(0) } // 0: Reservas, 1: Usuarios

    // Estados para los modales de confirmación
    var reservaParaCambioEstado by remember { mutableStateOf<Pair<String, EstadoReserva>?>(null) }
    var usuarioAEliminar by remember { mutableStateOf<String?>(null) }

    if (mostrarDialogoCrear) {
        DialogoCrearUsuario(viewModel) { mostrarDialogoCrear = false }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Panel Inspectoría", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { mostrarDialogoCrear = true }) { Text("+ Usuario") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = pestañaSeleccionada) {
            Tab(selected = pestañaSeleccionada == 0, onClick = { pestañaSeleccionada = 0 }, text = { Text("Reservas") })
            Tab(selected = pestañaSeleccionada == 1, onClick = { pestañaSeleccionada = 1 }, text = { Text("Usuarios") })
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (pestañaSeleccionada == 0) {
            // VISTA DE RESERVAS
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(reservas) { reserva ->
                    var menuExpandido by remember { mutableStateOf(false) }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${reserva.id} - ${reserva.solicitanteNombre}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                Box {
                                    AssistChip(onClick = { menuExpandido = true }, label = { Text(reserva.estado.displayName) })
                                    DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                                        EstadoReserva.values().forEach { estado ->
                                            DropdownMenuItem(
                                                text = { Text(estado.displayName) },
                                                onClick = {
                                                    // Interceptamos la acción y mostramos el modal
                                                    reservaParaCambioEstado = Pair(reserva.id, estado)
                                                    menuExpandido = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Espacio: ${reserva.espacio.displayName}", style = MaterialTheme.typography.bodyLarge)
                            Text("Fecha: ${reserva.fechaReserva} | Horario: ${reserva.horaInicio} - ${reserva.horaFin}", style = MaterialTheme.typography.bodyMedium)
                            Text("Motivo: ${reserva.motivo}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            // VISTA DE USUARIOS
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(usuarios) { usuario ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(usuario.nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Correo: ${usuario.correo}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Contraseña: ${usuario.contrasenia}", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Rol: ${usuario.rol.displayName}", style = MaterialTheme.typography.labelMedium)
                                }

                                Button(
                                    onClick = {
                                        // Interceptamos la acción de eliminar
                                        usuarioAEliminar = usuario.id
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Eliminar", color = MaterialTheme.colorScheme.onError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Confirmación o Justificación para Cambiar Estado de Reserva
    reservaParaCambioEstado?.let { (idReserva, nuevoEstado) ->
        var justificacion by remember { mutableStateOf("") }
        val requiereJustificacion = nuevoEstado == EstadoReserva.RECHAZADA || nuevoEstado == EstadoReserva.CANCELADA

        AlertDialog(
            onDismissRequest = { reservaParaCambioEstado = null },
            title = { Text(if (requiereJustificacion) "Justificar ${nuevoEstado.displayName}" else "Confirmar Acción") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Estás seguro de cambiar el estado de la reserva $idReserva a ${nuevoEstado.displayName}?")

                    if (requiereJustificacion) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = justificacion,
                            onValueChange = { justificacion = it },
                            label = { Text("Motivo / Justificación obligatoria") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.actualizarEstado(idReserva, nuevoEstado, justificacion)
                        reservaParaCambioEstado = null
                    },
                    enabled = !requiereJustificacion || justificacion.isNotBlank()
                ) {
                    Text("Confirmar", color = if (requiereJustificacion && justificacion.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaParaCambioEstado = null }) { Text("Cancelar") }
            }
        )
    }

    // Modal de Confirmación para Eliminar Usuario
    usuarioAEliminar?.let { idUsuario ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de que deseas eliminar a este usuario del sistema? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarUsuario(idUsuario)
                        usuarioAEliminar = null
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoCrearUsuario(viewModel: ReservaViewModel, onDismiss: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("@colegiomariapaz.cl") }
    var contrasenia by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.AUTORIZADO) }
    var expandedRol by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Usuario Institucional") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") })
                OutlinedTextField(value = contrasenia, onValueChange = { contrasenia = it }, label = { Text("Contraseña") })

                ExposedDropdownMenuBox(expanded = expandedRol, onExpandedChange = { expandedRol = it }) {
                    OutlinedTextField(
                        value = rolSeleccionado.displayName, onValueChange = {}, readOnly = true,
                        label = { Text("Rol") }, modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRol) }
                    )
                    ExposedDropdownMenu(expanded = expandedRol, onDismissRequest = { expandedRol = false }) {
                        RolUsuario.values().forEach { rol ->
                            DropdownMenuItem(text = { Text(rol.displayName) }, onClick = { rolSeleccionado = rol; expandedRol = false })
                        }
                    }
                }
                if (mensajeError.isNotEmpty()) Text(mensajeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                val resultado = viewModel.crearUsuario(nombre, correo, rolSeleccionado, contrasenia)
                if (resultado == "Éxito") onDismiss() else mensajeError = resultado
            }) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}