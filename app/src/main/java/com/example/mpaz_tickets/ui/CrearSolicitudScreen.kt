package com.example.mpaz_tickets.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mpaz_tickets.model.TipoEspacio
import com.example.mpaz_tickets.model.ReservaViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearReservaScreen(
    viewModel: ReservaViewModel,
    onReservaCreada: () -> Unit
) {
    var espacio by remember { mutableStateOf(TipoEspacio.LABORATORIO) }
    var fechaReserva by remember { mutableStateOf("") }


    // Nuevos campos requeridos por el caso institucional
    var actividadEspecifica by remember { mutableStateOf("") }
    var cursoGrupo by remember { mutableStateOf("") }
    var cantidadParticipantes by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    // Estado para controlar el modal de confirmación antes de enviar
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    // Bloques pedagógicos fijos de 45 minutos para el colegio
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

    // Configuración del Calendario Nativo de Android (Exigiendo 1 día de anticipación)
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1) // Suma 1 día para que el mínimo sea mañana
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val mesFormateado = String.format("%02d", selectedMonth + 1)
            val diaFormateado = String.format("%02d", selectedDay)
            fechaReserva = "$diaFormateado/$mesFormateado/$selectedYear"
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
        Text("Crear Solicitud de Reserva", style = MaterialTheme.typography.headlineMedium)

        // Selector de Espacio
        ExposedDropdownMenuBox(
            expanded = expandedEspacio,
            onExpandedChange = { expandedEspacio = it }
        ) {
            OutlinedTextField(
                value = espacio.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Espacio a reservar") },
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
                            espacio = esp
                            expandedEspacio = false
                        }
                    )
                }
            }
        }

        // Campo de Fecha interactivo con Calendario
        OutlinedTextField(
            value = fechaReserva,
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de Reserva") },
            placeholder = { Text("Seleccione una fecha") },
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
                    Icon(Icons.Default.DateRange, contentDescription = "Abrir Calendario")
                }
            }
        )

        // Selectores de Hora basados en bloques de 45 minutos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bloque de Inicio
            ExposedDropdownMenuBox(
                expanded = expandedInicio,
                onExpandedChange = { expandedInicio = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora Inicio") },
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
                            onClick = {
                                horaInicio = bloque
                                expandedInicio = false
                            }
                        )
                    }
                }
            }

            // Bloque de Fin
            ExposedDropdownMenuBox(
                expanded = expandedFin,
                onExpandedChange = { expandedFin = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = horaFin,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora Fin") },
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
                            onClick = {
                                horaFin = bloque
                                expandedFin = false
                            }
                        )
                    }
                }
            }
        }

        // 1. Actividad específica
        OutlinedTextField(
            value = actividadEspecifica,
            onValueChange = { actividadEspecifica = it },
            label = { Text("Actividad Específica (Ej: Taller de Programación)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 2. Curso o grupo participante
        OutlinedTextField(
            value = cursoGrupo,
            onValueChange = { cursoGrupo = it },
            label = { Text("Curso o Grupo Participante (Ej: 3ro Básico A)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 3. Cantidad aproximada de participantes (Validación numérica)
        OutlinedTextField(
            value = cantidadParticipantes,
            onValueChange = { if (it.all { char -> char.isDigit() }) cantidadParticipantes = it },
            label = { Text("Cantidad de Participantes") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 4. Motivo de la reserva
        OutlinedTextField(
            value = motivo,
            onValueChange = { if (it.length <= 500) motivo = it },
            label = { Text("Motivo de la reserva") },
            modifier = Modifier.fillMaxWidth().height(90.dp),
            maxLines = 3
        )

        // 5. Observaciones (Opcional)
        OutlinedTextField(
            value = observaciones,
            onValueChange = { if (it.length <= 300) observaciones = it },
            label = { Text("Observaciones (Opcional)") },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            maxLines = 2
        )

        // Muestra el mensaje de error si hay un conflicto de reserva duplicada
        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Botón de Envío (Validando que los campos clave no estén vacíos)
        val formularioValido = fechaReserva.isNotBlank() &&
                actividadEspecifica.isNotBlank() &&
                cursoGrupo.isNotBlank() &&
                cantidadParticipantes.isNotBlank() &&
                motivo.isNotBlank()

        Button(
            onClick = { mostrarDialogoConfirmacion = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = formularioValido
        ) {
            Text("Solicitar Reserva")
        }
    }

    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("Confirmar Solicitud") },
            text = {
                Text(
                    "¿Estás seguro de enviar la solicitud?\n\n" +
                            "• Espacio: ${espacio.displayName}\n" +
                            "• Fecha: $fechaReserva ($horaInicio - $horaFin)\n" +
                            "• Actividad: $actividadEspecifica\n" +
                            "• Curso: $cursoGrupo\n" +
                            "• Participantes: $cantidadParticipantes"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val partesInicio = horaInicio.split(" - ")
                        val partesFin = horaFin.split(" - ")

                        val resultado = viewModel.crearReserva(
                            espacio = espacio,
                            motivo = motivo,
                            fechaReserva = fechaReserva,
                            horaInicio = partesInicio[0],
                            horaFin = partesFin[1],
                            actividadEspecifica = actividadEspecifica,
                            cursoGrupo = cursoGrupo,
                            cantidadParticipantesStr = cantidadParticipantes
                        )

                        mostrarDialogoConfirmacion = false

                        if (resultado == "Éxito") {
                            onReservaCreada()
                        } else {
                            // Muestra el error (ya sea por capacidad excedida o por duplicado) en letras rojas
                            mensajeError = resultado
                        }
                    }
                ) {
                    Text("Sí, enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}