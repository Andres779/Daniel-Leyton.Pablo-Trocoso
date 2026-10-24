package com.example.mpaz_tickets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mpaz_tickets.model.RolUsuario
import com.example.mpaz_tickets.model.ReservaViewModel
import com.example.mpaz_tickets.ui.*
import com.example.mpaz_tickets.ui.theme.MPAZ_ticketsTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.mpaz_tickets.data.AppDatabase
import com.example.mpaz_tickets.data.ReservasRepository



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //ACA CONSTRUYO MI BASE DE DATOS LOCAL:
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "reservas.db"
        ).build()

        //ACA OBTENGO EL DAO PARA PASARLO AL REPOSITORIO
        val dao = db.reservasDao()
        val repository = ReservasRepository(dao)

        //Y por ultimo creo un Factory para inyectar el repositorio en el ViewModel
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ReservaViewModel(repository) as T
            }
        }

        // Aca instancio mi ViewModel utilizando el Factory
        val viewModel = ViewModelProvider(this, factory)[ReservaViewModel::class.java]

        setContent {
            MPAZ_ticketsTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

// 1. Se actualizan las rutas para reflejar las "Reservas"
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object CrearReserva : Screen("crear_reserva")
    object MisReservas : Screen("mis_reservas")
    object Catalogo : Screen("catalogo")
    object BloquearEspacio : Screen("bloquear_espacio")
    object AdminDashboard : Screen("admin_dashboard")
    object Historial : Screen("historial")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: ReservaViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val usuarioActual by viewModel.usuarioActual.collectAsState()

    val mostrarElementosNavegacion = currentRoute != Screen.Login.route && usuarioActual != null

    Scaffold(
        topBar = {
            if (mostrarElementosNavegacion) {
                val notificaciones by viewModel.notificaciones.collectAsState()
                var menuNotificacionesExpandido by remember { mutableStateOf(false) }
                val noLeidas = notificaciones.count { !it.leida }

                TopAppBar(
                    title = { Text("MPAZ Reservas") },
                    actions = {
                        // Ícono de Notificaciones con Campana y Badge
                        Box {
                            IconButton(onClick = {
                                menuNotificacionesExpandido = true
                                viewModel.marcarNotificacionesComoLeidas()
                            }) {
                                BadgedBox(
                                    badge = {
                                        if (noLeidas > 0) {
                                            Badge { Text("$noLeidas") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                                }
                            }

                            // Menú desplegable con el historial de notificaciones in-app
                            DropdownMenu(
                                expanded = menuNotificacionesExpandido,
                                onDismissRequest = { menuNotificacionesExpandido = false },
                                modifier = Modifier.width(300.dp).heightIn(max = 400.dp)
                            ) {
                                Text(
                                    "Notificaciones",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                                HorizontalDivider()

                                if (notificaciones.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No hay notificaciones", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        onClick = {}
                                    )
                                } else {
                                    notificaciones.forEach { noti ->
                                        DropdownMenuItem(
                                            text = {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(noti.titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                                    Text(noti.mensaje, style = MaterialTheme.typography.bodySmall)
                                                    Text(noti.fechaHora, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                                }
                                            },
                                            onClick = {}
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }

                        // Botón de Cerrar Sesión
                        IconButton(onClick = {
                            viewModel.cerrarSesion()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (mostrarElementosNavegacion) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigate(Screen.Home.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                        label = { Text("Crear") },
                        selected = currentRoute == Screen.CrearReserva.route,
                        onClick = { navController.navigate(Screen.CrearReserva.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Mis Reservas") },
                        label = { Text("Reservas") }, // 3. Etiqueta actualizada
                        selected = currentRoute == Screen.MisReservas.route,
                        onClick = { navController.navigate(Screen.MisReservas.route) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Catálogo") }, // Cambiamos el ícono por uno de lista
                        label = { Text("Catálogo") },
                        selected = currentRoute == Screen.Catalogo.route,
                        onClick = { navController.navigate(Screen.Catalogo.route) } // ¡Corregido! Ahora va a Catalogo
                    )

                    // 4. Actualizado para usar el rol de Inspectoría (dejé ADMINISTRADOR como fallback por si acaso)
                    if (usuarioActual?.rol == RolUsuario.INSPECTORIA || usuarioActual?.rol == RolUsuario.INSPECTORIA) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "Inspectoría") },
                            label = { Text("Inspectoría") }, // 5. Etiqueta actualizada
                            selected = currentRoute == Screen.AdminDashboard.route,
                            onClick = { navController.navigate(Screen.AdminDashboard.route) }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                            label = { Text("Historial") },
                            selected = currentRoute == Screen.Historial.route,
                            onClick = { navController.navigate(Screen.Historial.route) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) { LoginScreen(viewModel, navController) }

            // 6. HomeScreen ya NO recibe el parámetro del botón que eliminamos
            composable(Screen.Home.route) { HomeScreen(viewModel) }

            // 7. Llamada a la nueva pantalla CrearReservaScreen
            composable(Screen.CrearReserva.route) {
                CrearReservaScreen(viewModel, onReservaCreada = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                })
            }

            composable(Screen.BloquearEspacio.route) {
                BloquearEspacioScreen(viewModel) {
                    navController.popBackStack()
                }
            }

            // 8. Llamada a la nueva pantalla MisReservasScreen
            composable(Screen.MisReservas.route) { MisReservasScreen(viewModel) }
            composable(Screen.Catalogo.route) { CatalogoScreen(viewModel) }

            composable(Screen.AdminDashboard.route) { AdminDashboardScreen(viewModel) }
            composable(Screen.Historial.route) { HistorialScreen(viewModel) }
        }
    }
}