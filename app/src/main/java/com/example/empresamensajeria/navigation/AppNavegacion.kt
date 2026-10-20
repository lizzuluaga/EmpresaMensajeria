package com.example.empresamensajeria.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.empresamensajeria.ui.screens.ContactoScreen
import com.example.empresamensajeria.ui.screens.CotizarScreen
import com.example.empresamensajeria.ui.screens.LoginScreen
import com.example.empresamensajeria.ui.screens.RastrearScreen
import com.example.empresamensajeria.ui.screens.ServiciosScreen
import kotlinx.coroutines.launch

private data class OpcionMenu(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val abrirMenu: () -> Unit = { scope.launch { drawerState.open() } }

    val opciones = listOf(
        OpcionMenu(Rutas.SERVICIOS, "Nuestros servicios", Icons.Default.Home),
        OpcionMenu(Rutas.RASTREAR, "Rastrea tu pedido", Icons.Default.LocationOn),
        OpcionMenu(Rutas.CONTACTO, "Contáctanos", Icons.Default.Phone),
        OpcionMenu(Rutas.LOGIN, "Iniciar sesión", Icons.Default.Person)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "EnviaYa",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                for (opcion in opciones) {
                    NavigationDrawerItem(
                        label = { Text(opcion.titulo) },
                        icon = { Icon(opcion.icono, contentDescription = null) },
                        selected = rutaActual == opcion.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(opcion.ruta) {
                                popUpTo(Rutas.SERVICIOS)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Rutas.SERVICIOS
        ) {
            composable(Rutas.SERVICIOS) {
                ServiciosScreen (
                    onMenuClick = abrirMenu,
                    onServicioClick = { id -> navController.navigate(Rutas.cotizar(id)) }
                )
            }
            composable(
                route = Rutas.COTIZAR,
                arguments = listOf(navArgument("servicioId") { type = NavType.IntType })
            ) { entrada ->
                val servicioId = entrada.arguments?.getInt("servicioId") ?: 0
                CotizarScreen(servicioId = servicioId, onMenuClick = abrirMenu)
            }
            composable(Rutas.RASTREAR) { RastrearScreen(abrirMenu) }
            composable(Rutas.CONTACTO) { ContactoScreen(abrirMenu) }
            composable(Rutas.LOGIN) { LoginScreen(abrirMenu) }
        }
    }
}