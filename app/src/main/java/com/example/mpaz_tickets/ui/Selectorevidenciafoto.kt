package com.example.mpaz_tickets.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun SelectorEvidenciaFoto(viewModel: ReservaViewModel, etiqueta: String) {
    val context = LocalContext.current
    val foto by viewModel.fotoEvidencia.collectAsState()

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.guardarFotoEvidencia(bitmap)
        }
    }

    fun abrirCamara() {
        try {
            camaraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay una app de cámara disponible", Toast.LENGTH_SHORT).show()
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium)

        foto?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto de evidencia del espacio",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            )
        }

        OutlinedButton(
            onClick = {
                val tienePermiso = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (tienePermiso) abrirCamara()
                else permisoLauncher.launch(Manifest.permission.CAMERA)
            }
        ) {
            Text(if (foto == null) "Tomar foto" else "Repetir foto")
        }
    }
}