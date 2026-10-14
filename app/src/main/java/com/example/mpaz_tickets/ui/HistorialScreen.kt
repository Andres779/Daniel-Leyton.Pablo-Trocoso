package com.example.mpaz_tickets.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun HistorialScreen(viewModel: ReservaViewModel) {
    val historial by viewModel.historial.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Títulos sin el botón de limpiar
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Historial de Auditoría", style = MaterialTheme.typography.headlineMedium)
            Text("Registro cronológico de acciones.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(historial) { evento ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(evento.usuario, style = MaterialTheme.typography.titleSmall)
                            Text(evento.fechaHora, style = MaterialTheme.typography.labelSmall)
                        }
                        Text("${evento.accion}: ${evento.detalle}")
                    }
                }
            }
        }
    }
}