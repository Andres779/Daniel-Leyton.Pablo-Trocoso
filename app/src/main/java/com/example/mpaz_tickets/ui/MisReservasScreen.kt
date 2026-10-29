package com.example.mpaz_tickets.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mpaz_tickets.model.EstadoReserva
import com.example.mpaz_tickets.model.Reserva
import com.example.mpaz_tickets.model.ReservaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReservasScreen(viewModel: ReservaViewModel) {
    val reservasGlobales by viewModel.reservas.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val fotoEvidencia by viewModel.fotoEvidencia.collectAsState()
    val context = LocalContext.current

    val reservas = remember(reservasGlobales, usuarioActual) {
        viewModel.obtenerReservasPersonales()
    }

    // Guardamos solo el ID y buscamos la reserva en la lista actual:
    // así, si Inspectoría cambia el estado, el panel se actualiza solo.
    var reservaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    val reservaSeleccionada = reservas.find { it.id == reservaSeleccionadaId }
    var reservaACancelar by remember { mutableStateOf<Reserva?>(null) }

    // 1. Lanzador de la cámara nativa
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Si el usuario cancela, bitmap es null: NO borramos la foto anterior
        if (bitmap != null) viewModel.guardarFotoEvidencia(bitmap)
    }

    fun abrirCamara() {
        try {
            cameraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, "Falta el permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Lanzador de permiso de cámara
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    fun pedirCamara() {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermiso) abrirCamara() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // 3. Calendario nativo
    fun agendarEnCalendario(reserva: Reserva) {
        try {
            val intent = viewModel.obtenerIntentCalendarioNativo(
                espacioNombre = reserva.espacio.displayName,
                actividad = reserva.actividadEspecifica,
                motivo = reserva.motivo,
                fecha = reserva.fechaReserva,
                horaInicio = reserva.horaInicio,
                horaFin = reserva.horaFin
            )
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app de calendario instalada", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir el calendario", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Gestión de Reservas", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(reservas, key = { it.id }) { reserva ->
                // Animacion: el color del estado cambia suavemente
                val colorEstado by animateColorAsState(
                    targetValue = when (reserva.estado) {
                        EstadoReserva.APROBADA -> MaterialTheme.colorScheme.primary
                        EstadoReserva.RECHAZADA, EstadoReserva.CANCELADA -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    animationSpec = tween(durationMillis = 600),
                    label = "colorEstado"
                )
                Card(
                    onClick = { reservaSeleccionadaId = reserva.id },
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
                            Text(reserva.estado.displayName, fontWeight = FontWeight.Bold, color = colorEstado)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Solicitante: ${reserva.solicitanteNombre}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: ${reserva.fechaReserva} | ${reserva.horaInicio} - ${reserva.horaFin}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        reservaSeleccionada?.let { selected ->
          // Animaciobn2: el panel de opciones aparece desplegándose con fundido.
          key(selected.id) {
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
                enter = expandVertically(animationSpec = tween(350)) + fadeIn(animationSpec = tween(350))
            ) {
            Column {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Panel con scroll para que los botones nunca queden fuera de pantalla
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Opciones para la reserva: ${selected.id}", style = MaterialTheme.typography.titleMedium)

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

                    // Funciones nativas: solo para reservas aprobadas
                    if (selected.estado == EstadoReserva.APROBADA) {
                        Button(
                            onClick = { agendarEnCalendario(selected) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Agendar")
                        }

                        Button(
                            onClick = { pedirCamara() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Cámara")
                        }
                    }
                }

                // Aviso para que se entienda por qué no aparecen Agendar / Cámara
                if (selected.estado == EstadoReserva.PENDIENTE) {
                    Text(
                        text = "Las opciones Agendar y Cámara se habilitan cuando Inspectoría apruebe la reserva.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Previsualización de la foto tomada
                fotoEvidencia?.let { bitmap ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Evidencia capturada:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Evidencia de estado del espacio",
                        contentScale = ContentScale.Fit,
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
                    onClick = { reservaSeleccionadaId = null },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Cerrar")
                }
            }
            }
            }
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
                        reservaSeleccionadaId = null
                    }
                ) {
                    Text("Sí, cancelar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaACancelar = null }) {
                    Text("Volver")
                }
            }
        )
    }
}
