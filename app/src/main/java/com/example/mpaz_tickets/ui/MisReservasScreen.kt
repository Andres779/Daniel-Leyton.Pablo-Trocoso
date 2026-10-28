package com.example.mpaz_tickets.ui

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.EstadoReserva
import com.example.mpaz_tickets.model.Reserva
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun MisReservasScreen(viewModel: ReservaViewModel) {
    val reservasGlobales by viewModel.reservas.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val fotoEvidencia by viewModel.fotoEvidencia.collectAsState()
    val context = LocalContext.current

    val reservas = remember(reservasGlobales, usuarioActual) {
        viewModel.obtenerReservasPersonales()
    }

    var reservaSeleccionada by remember { mutableStateOf<Reserva?>(null) }
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }

    // Lanzador para la cámara nativa
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        viewModel.guardarFotoEvidencia(bitmap)
    }

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

            Text("Opciones para la reserva: ${selected.id}", style = MaterialTheme.typography.titleMedium)

            // Fila de botones dinámicos según el estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (selected.estado == EstadoReserva.PENDIENTE || selected.estado == EstadoReserva.APROBADA) {
                    Button(
                        onClick = { reservaACancelar = selected },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancelar")
                    }
                }

                // Opciones exclusivas para reservas aprobadas
                if (selected.estado == EstadoReserva.APROBADA) {
                    Button(
                        onClick = {
                            val intent = viewModel.obtenerIntentCalendarioNativo(
                                espacioNombre = selected.espacio.displayName,
                                actividad = selected.actividadEspecifica,
                                motivo = selected.motivo,
                                fecha = selected.fechaReserva,
                                horaInicio = selected.horaInicio,
                                horaFin = selected.horaFin
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Agendar")
                    }

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Cámara")
                    }
                }
            }

            // Previsualización de la foto tomada
            fotoEvidencia?.let { bitmap ->
                Spacer(modifier = Modifier.height(16.dp))
                Text("Evidencia capturada:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Evidencia de estado del espacio",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 8.dp)
                )
                TextButton(
                    onClick = { viewModel.limpiarFotoEvidencia() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Borrar foto", color = MaterialTheme.colorScheme.error)
                }
            }

            if (selected.estado != EstadoReserva.PENDIENTE && selected.estado != EstadoReserva.APROBADA) {
                Text(
                    text = "Esta reserva se encuentra ${selected.estado.displayName.lowercase()} y no puede ser modificada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = { reservaSeleccionada = null },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Cerrar")
            }
        }
    }

    // --- MODAL DE CONFIRMACIÓN DE CANCELACIÓN ---
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
                        reservaSeleccionada = null
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