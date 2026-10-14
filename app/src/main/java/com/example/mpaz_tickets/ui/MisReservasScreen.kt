package com.example.mpaz_tickets.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.EstadoReserva
import com.example.mpaz_tickets.model.Reserva
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun MisReservasScreen(viewModel: ReservaViewModel) {
    val reservasGlobales by viewModel.reservas.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()

    val reservas = remember(reservasGlobales, usuarioActual) {
        viewModel.obtenerReservasPersonales()
    }

    var reservaSeleccionada by remember { mutableStateOf<Reserva?>(null) }
    // Estado para controlar la visibilidad del modal de confirmación
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Gestión de Reservas", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(reservas) { reserva ->
                Card(
                    onClick = { reservaSeleccionada = reserva },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(reserva.espacio.displayName, style = MaterialTheme.typography.titleMedium)
                            Text(reserva.estado.displayName, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Solicitante: ${reserva.solicitanteNombre}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: ${reserva.fechaReserva} | ${reserva.horaInicio} - ${reserva.horaFin}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        reservaSeleccionada?.let { selected ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Validación: Solo se puede cancelar si está PENDIENTE o APROBADA
            if (selected.estado == EstadoReserva.PENDIENTE || selected.estado == EstadoReserva.APROBADA) {
                Text("Opciones para la reserva: ${selected.id}", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            // En lugar de cancelar directo, guardamos la reserva para activar el AlertDialog
                            reservaACancelar = selected
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancelar mi reserva")
                    }
                }
            } else {
                Text(
                    text = "Esta reserva se encuentra ${selected.estado.displayName} y no puede ser modificada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = { reservaSeleccionada = null },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }

    // --- MODAL DE CONFIRMACIÓN (AlertDialog) ---
    reservaACancelar?.let { reserva ->
        AlertDialog(
            onDismissRequest = { reservaACancelar = null },
            title = { Text("Confirmar Cancelación") },
            text = { Text("¿Estás seguro de que deseas cancelar tu reserva para ${reserva.espacio.displayName} el día ${reserva.fechaReserva}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.actualizarEstado(reserva.id, EstadoReserva.CANCELADA)
                        reservaACancelar = null
                        reservaSeleccionada = null // Limpia la selección de la vista principal
                    }
                ) {
                    Text("Sí, cancelar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { reservaACancelar = null }
                ) {
                    Text("Volver")
                }
            }
        )
    }
}