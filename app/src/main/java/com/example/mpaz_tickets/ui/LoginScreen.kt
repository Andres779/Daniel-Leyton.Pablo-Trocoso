package com.example.mpaz_tickets.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mpaz_tickets.Screen
import com.example.mpaz_tickets.model.ReservaViewModel

@Composable
fun LoginScreen(viewModel: ReservaViewModel, navController: NavController) {
    // Los campos ahora inician vacíos
    var correo by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Títulos adaptados al nuevo contexto de reservas
        Text("MPAZ Reservas", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Gestión de espacios institucionales", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it; error = false },
            label = { Text("Correo Institucional") },
            placeholder = { Text("ejemplo@colegiomariapaz.cl") }, // Añadimos un texto de ayuda
            modifier = Modifier.fillMaxWidth(),
            isError = error
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasenia,
            onValueChange = { contrasenia = it; error = false },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error
        )

        if (error) {
            Text("Credenciales incorrectas", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.iniciarSesion(correo, contrasenia)) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    error = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            // Evita que el usuario haga clic si los campos están vacíos
            enabled = correo.isNotBlank() && contrasenia.isNotBlank()
        ) {
            Text("Iniciar Sesión")
        }
    }
}