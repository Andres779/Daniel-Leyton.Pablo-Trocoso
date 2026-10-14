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
import com.example.mpaz_tickets.model.ReservaViewModel
import com.example.mpaz_tickets.model.RolUsuario

@Composable
fun CatalogoScreen(viewModel: ReservaViewModel) {
    val usuarioActual by viewModel.usuarioActual.collectAsState()
    val listaEspacios by viewModel.espaciosDetalle.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Catálogo de Espacios", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text("Consulta la disponibilidad y características de los recintos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(listaEspacios) { espacio ->
                var menuExpandido by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(espacio.tipo.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                            val colorEstado = when (espacio.estado) {
                                "Disponible" -> MaterialTheme.colorScheme.primary
                                "Ocupado" -> MaterialTheme.colorScheme.tertiary
                                "En Mantenimiento" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                            Text(espacio.estado, style = MaterialTheme.typography.labelLarge, color = colorEstado)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(espacio.descripcion, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📍 Ubicación: ${espacio.ubicacion}", style = MaterialTheme.typography.bodySmall)
                            Text("👥 Capacidad: ${espacio.capacidad} pers.", style = MaterialTheme.typography.bodySmall)
                        }

                        if (usuarioActual?.rol == RolUsuario.INSPECTORIA) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                OutlinedButton(
                                    onClick = { menuExpandido = true },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Gestionar Estado")
                                }

                                DropdownMenu(
                                    expanded = menuExpandido,
                                    onDismissRequest = { menuExpandido = false }
                                ) {
                                    val estadosPosibles = listOf("Disponible", "Ocupado", "En Mantenimiento", "Fuera de Servicio")
                                    estadosPosibles.forEach { nuevoEstado ->
                                        DropdownMenuItem(
                                            text = { Text(nuevoEstado) },
                                            onClick = {
                                                viewModel.actualizarEstadoEspacio(espacio.tipo, nuevoEstado)
                                                menuExpandido = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}