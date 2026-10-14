package com.example.mpaz_tickets.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.EstadoReserva
import com.example.mpaz_tickets.model.RolUsuario
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun HomeScreen(
    viewModel: ReservaViewModel
    // Eliminamos el onCrearSolicitudClick para evitar errores de UI
    ) {
    val reservasGlobales by viewModel.reservas.collectAsState()
    val usuarioActual by viewModel.usuarioActual.collectAsState()

    // Filtramos las reservas según el rol del usuario
    val reservas = remember(reservasGlobales, usuarioActual) {
        viewModel.obtenerReservasPersonales()
    }

    val total = reservas.size
    val pendientes = reservas.count { it.estado == EstadoReserva.PENDIENTE }
    val aprobadas = reservas.count { it.estado == EstadoReserva.APROBADA }
    val rechazadas = reservas.count { it.estado == EstadoReserva.RECHAZADA }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Al quitar el botón, también eliminamos el Row que rompía la pantalla
        Column(modifier = Modifier.fillMaxWidth()) {
            // Se añade el nombre del usuario al mensaje de bienvenida
            val nombreUsuario = usuarioActual?.nombre ?: "Usuario"
            Text("¡Hola, $nombreUsuario!", style = MaterialTheme.typography.headlineMedium)
            Text("Aquí tienes un resumen de tus reservas.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjetas de Métricas actualizadas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricCard("Total", total.toString())
            MetricCard("Pendientes", pendientes.toString())
            MetricCard("Aprobadas", aprobadas.toString())
            MetricCard("Rechazadas", rechazadas.toString())
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Tus últimas reservas", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(reservas.take(5)) { reserva ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Muestra el ID y el espacio reservado
                            Text("${reserva.id} - ${reserva.espacio.displayName}", style = MaterialTheme.typography.titleSmall)
                            // Muestra la fecha y el horario
                            Text("${reserva.fechaReserva} (${reserva.horaInicio} - ${reserva.horaFin})", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(reserva.estado.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, count: String) {
    Card(modifier = Modifier.size(width = 85.dp, height = 70.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall)
            Text(count, style = MaterialTheme.typography.titleMedium)
        }
    }
}