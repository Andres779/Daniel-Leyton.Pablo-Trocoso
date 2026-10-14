package com.example.mpaz_tickets.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.TipoEspacio
import com.example.mpaz_tickets.model.ReservaViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloquearEspacioScreen(
    viewModel: ReservaViewModel,
    onBloqueoRealizado: () -> Unit
) {
    var espacioSeleccionado by remember { mutableStateOf(TipoEspacio.LABORATORIO) }
    var fechaBloqueo by remember { mutableStateOf("") }
    var motivoBloqueo by remember { mutableStateOf("") }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // Bloques pedagógicos fijos de 45 minutos
    val bloquesHorarios = listOf(
        "08:00 - 08:45",
        "08:45 - 09:30",
        "09:45 - 10:30",
        "10:30 - 11:15",
        "11:30 - 12:15",
        "12:15 - 13:00",
        "14:00 - 14:45",
        "14:45 - 15:30"
    )

    var horaInicio by remember { mutableStateOf(bloquesHorarios[0]) }
    var horaFin by remember { mutableStateOf(bloquesHorarios[1]) }

    var expandedEspacio by remember { mutableStateOf(false) }
    var expandedInicio by remember { mutableStateOf(false) }
    var expandedFin by remember { mutableStateOf(false) }

    // Configuración del Selector de Fecha Nativo
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val mesFormateado = String.format("%02d", selectedMonth + 1)
            val diaFormateado = String.format("%02d", selectedDay)
            fechaBloqueo = "$diaFormateado/$mesFormateado/$selectedYear"
        },
        year,
        month,
        day
    ).apply {
        datePicker.minDate = calendar.timeInMillis
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Bloqueo de Espacios (Inspectoría)", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Text("Restringe el uso de recintos por mantenimiento o eventos institucionales.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de Espacio a Bloquear
        ExposedDropdownMenuBox(
            expanded = expandedEspacio,
            onExpandedChange = { expandedEspacio = it }
        ) {
            OutlinedTextField(
                value = espacioSeleccionado.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Recinto a Bloquear") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEspacio) }
            )
            ExposedDropdownMenu(
                expanded = expandedEspacio,
                onDismissRequest = { expandedEspacio = false }
            ) {
                TipoEspacio.values().forEach { esp ->
                    DropdownMenuItem(
                        text = { Text(esp.displayName) },
                        onClick = {
                            espacioSeleccionado = esp
                            expandedEspacio = false
                        }
                    )
                }
            }
        }

        // Campo de Fecha
        OutlinedTextField(
            value = fechaBloqueo,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de Bloqueo") },
            placeholder = { Text("Seleccione la fecha") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendario")
                }
            }
        )

        // Selectores de Hora (Inicio y Fin)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedInicio,
                onExpandedChange = { expandedInicio = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Desde") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInicio) }
                )
                ExposedDropdownMenu(
                    expanded = expandedInicio,
                    onDismissRequest = { expandedInicio = false }
                ) {
                    bloquesHorarios.forEach { bloque ->
                        DropdownMenuItem(
                            text = { Text(bloque) },
                            onClick = { horaInicio = bloque; expandedInicio = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedFin,
                onExpandedChange = { expandedFin = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = horaFin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hasta") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFin) }
                )
                ExposedDropdownMenu(
                    expanded = expandedFin,
                    onDismissRequest = { expandedFin = false }
                ) {
                    bloquesHorarios.forEach { bloque ->
                        DropdownMenuItem(
                            text = { Text(bloque) },
                            onClick = { horaFin = bloque; expandedFin = false }
                        )
                    }
                }
            }
        }

        // Motivo del Bloqueo
        OutlinedTextField(
            value = motivoBloqueo,
            onValueChange = { if (it.length <= 250) motivoBloqueo = it },
            label = { Text("Motivo del Bloqueo (Ej: Mantenimiento de Redes)") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 3
        )

        // Botón para aplicar el bloqueo
        Button(
            onClick = { mostrarConfirmacion = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            enabled = fechaBloqueo.isNotBlank() && motivoBloqueo.isNotBlank()
        ) {
            Text("Bloquear Recinto", color = MaterialTheme.colorScheme.onError)
        }
    }

    // Modal de Confirmación de Bloqueo
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar Bloqueo de Espacio") },
            text = {
                Text(
                    "¿Estás seguro de bloquear el recinto '${espacioSeleccionado.displayName}'?\n\n" +
                            "• Fecha: $fechaBloqueo\n" +
                            "• Horario: $horaInicio - $horaFin\n" +
                            "• Motivo: $motivoBloqueo"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val partesInicio = horaInicio.split(" - ")
                        val partesFin = horaFin.split(" - ")

                        // Registramos el bloqueo mediante el ViewModel
                        viewModel.bloquearEspacio(
                            espacio = espacioSeleccionado,
                            fecha = fechaBloqueo,
                            horaInicio = partesInicio[0],
                            horaFin = partesFin[1],
                            motivo = motivoBloqueo
                        )
                        mostrarConfirmacion = false
                        onBloqueoRealizado()
                    }
                ) {
                    Text("Sí, bloquear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}