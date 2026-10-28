package com.example.mpaz_tickets.ui

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.mpaz_tickets.model.Reserva
import com.example.mpaz_tickets.model.ReservaViewModel
import android.content.Intent
import android.provider.CalendarContract

@Composable
fun BotonExportarCalendario(viewModel: ReservaViewModel, reserva: Reserva) {
    val context = LocalContext.current

    Button(onClick = {
        val intent = viewModel.obtenerIntentCalendarioNativo(
            espacioNombre = reserva.espacio.displayName,
            actividad = reserva.actividadEspecifica,
            motivo = reserva.motivo,
            fecha = reserva.fechaReserva,
            horaInicio = reserva.horaInicio,
            horaFin = reserva.horaFin
        )
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app de calendario instalada", Toast.LENGTH_SHORT).show()
        }
    }) {
        Text("Añadir a mi Calendario")
    }
}