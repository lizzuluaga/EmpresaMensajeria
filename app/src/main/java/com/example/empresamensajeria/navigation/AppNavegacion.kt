package com.example.empresamensajeria.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.empresamensajeria.data.ModoCotizacion
import com.example.empresamensajeria.data.listaServicios
import com.example.empresamensajeria.data.session.Rol
import com.example.empresamensajeria.data.session.Sesion
import com.example.empresamensajeria.ui.screens.ContactoScreen
import com.example.empresamensajeria.ui.screens.CotizarScreen
import com.example.empresamensajeria.ui.screens.InventarioScreen
import com.example.empresamensajeria.ui.screens.LoginScreen
import com.example.empresamensajeria.ui.screens.MiBodegaScreen
import com.example.empresamensajeria.ui.screens.PanelAdminScreen
import com.example.empresamensajeria.ui.screens.RastrearScreen
import com.example.empresamensajeria.ui.screens.ServiciosScreen
import com.example.empresamensajeria.ui.viewmodel.EstadoSesion
import com.example.empresamensajeria.ui.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

private data class OpcionMenu(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
)

@Composable
fun AppNavegacion(
    sesionViewModel: SesionViewModel = viewModel(factory = SesionViewModel.Factory)
) {
    val estadoSesion by sesionViewModel.estado.collectAsStateWithLifecycle()

    when (val estado = estadoSesion) {
        // Mientras se lee la sesión guardada solo se ve un indicador de carga,
        // así no aparece una pantalla que enseguida se cambia por otra
        EstadoSesion.Cargando -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is EstadoSesion.Lista -> ContenidoApp(
            sesion = estado.sesion,
            onCerrarSesion = sesionViewModel::cerrarSesion
        )
    }
}

@Composable
private fun ContenidoApp(
    sesion: Sesion?,
    onCerrarSesion: () -> Unit
) {
    // Pantalla inicial: si la app se abre con una sesión guardada, va directo a su panel.
    // Se calcula una sola vez, al abrir la app.
    val inicio = remember { sesion?.let { Rutas.panelDe(it.rol) } ?: Rutas.SERVICIOS }

    NavegacionPrincipal(
        inicio = inicio,
        sesion = sesion,
        onBorrarSesion = onCerrarSesion
    )
}

@Composable
private fun NavegacionPrincipal(
    inicio: String,
    sesion: Sesion?,
    onBorrarSesion: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    val abrirMenu: () -> Unit = { scope.launch { drawerState.open() } }

    // Cerrar sesión: borra la sesión y deja la pila como [Servicios, Login], con Login en la
    // pestaña del rol que cerró. Atrás desde Login lleva a Servicios, no al panel.
    val onCerrarSesion: () -> Unit = {
        val rol = sesion?.rol ?: Rol.CLIENTE
        onBorrarSesion()
        navController.navigate(Rutas.SERVICIOS) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        navController.navigate(Rutas.login(rol))
    }

    // La última opción depende de la sesión: Iniciar sesión, Mi bodega o Panel admin
    val opcionCuenta = when (sesion?.rol) {
        Rol.CLIENTE -> OpcionMenu(Rutas.MI_BODEGA, "Mi bodega", Icons.Default.Person)
        Rol.ADMIN -> OpcionMenu(Rutas.PANEL_ADMIN, "Panel admin", Icons.Default.Settings)
        null -> OpcionMenu(Rutas.LOGIN, "Iniciar sesión", Icons.Default.Person)
    }
    val opciones = listOf(
        OpcionMenu(Rutas.SERVICIOS, "Nuestros servicios", Icons.Default.Home),
        OpcionMenu(Rutas.RASTREAR, "Rastrea tu pedido", Icons.Default.LocationOn),
        OpcionMenu(Rutas.CONTACTO, "Contáctanos", Icons.Default.Phone),
        opcionCuenta
    )

    // Desde Cotizar: con sesión activa se va al panel en vez de al login
    val irALogin: () -> Unit = {
        navController.irA(sesion?.let { Rutas.panelDe(it.rol) } ?: Rutas.LOGIN)
    }

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
                sesion?.let {
                    Text(
                        text = it.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                for (opcion in opciones) {
                    NavigationDrawerItem(
                        label = { Text(opcion.titulo) },
                        icon = { Icon(opcion.icono, contentDescription = null) },
                        // Sin la parte "?guia=..." para que Rastrear siga marcado en el menú
                        selected = rutaActual?.substringBefore('?') == opcion.ruta,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.irA(opcion.ruta)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = inicio
        ) {
            composable(Rutas.SERVICIOS) {
                ServiciosScreen(
                    onMenuClick = abrirMenu,
                    onServicioClick = { id -> navController.navigate(Rutas.cotizar(id)) }
                )
            }
            composable(
                route = Rutas.COTIZAR,
                arguments = listOf(navArgument("servicioId") { type = NavType.IntType })
            ) { entrada ->
                val servicioId = entrada.arguments?.getInt("servicioId") ?: 0
                CotizarScreen(
                    servicioId = servicioId,
                    onMenuClick = abrirMenu,
                    onRastrearGuia = { guia -> navController.irA(Rutas.rastrear(guia)) },
                    onIrALogin = irALogin,
                    documentoSesionCliente = sesion
                        ?.takeIf { it.rol == Rol.CLIENTE }
                        ?.identificador
                )
            }
            // "guia" es opcional: desde el menú llega vacía y RastrearViewModel no busca nada
            composable(
                route = Rutas.RASTREAR_CON_GUIA,
                arguments = listOf(navArgument(Rutas.ARG_GUIA) {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { RastrearScreen(abrirMenu) }
            composable(Rutas.CONTACTO) { ContactoScreen(abrirMenu) }
            // "pestana" es opcional: desde el menú llega vacía y se abre la pestaña Cliente
            composable(
                route = Rutas.LOGIN_CON_PESTANA,
                arguments = listOf(navArgument(Rutas.ARG_PESTANA) {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) {
                LoginScreen(
                    onMenuClick = abrirMenu,
                    onSesionIniciada = { rol ->
                        // Se quita Login de la pila: el botón atrás no vuelve al formulario
                        navController.navigate(Rutas.panelDe(rol)) {
                            popUpTo(Rutas.LOGIN_CON_PESTANA) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Rutas.MI_BODEGA) {
                MiBodegaScreen(
                    onMenuClick = abrirMenu,
                    onCerrarSesion = onCerrarSesion,
                    onContratarOtro = {
                        // Cotizar con el servicio de bodega ya seleccionado
                        val bodega = listaServicios.first { it.modo == ModoCotizacion.BODEGA }
                        navController.navigate(Rutas.cotizar(bodega.id))
                    },
                    onAbrirContrato = { id -> navController.navigate(Rutas.inventario(id)) }
                )
            }
            composable(
                route = Rutas.INVENTARIO,
                arguments = listOf(navArgument(Rutas.ARG_CONTRATO_ID) { type = NavType.LongType })
            ) {
                InventarioScreen(onAtrasClick = { navController.popBackStack() })
            }
            composable(Rutas.PANEL_ADMIN) {
                PanelAdminScreen(
                    onMenuClick = abrirMenu,
                    onCerrarSesion = onCerrarSesion,
                    onVerInventario = { id -> navController.navigate(Rutas.inventario(id)) }
                )
            }
        }
    }
}

/**
 * Navegación de nivel superior (menú lateral y accesos directos): conserva la pantalla que está
 * en la base de la pila (Servicios, o el panel si la app se abrió con sesión) y reemplaza lo demás.
 * Si el destino es esa misma pantalla base, simplemente vuelve a ella para no apilar copias.
 */
private fun NavHostController.irA(ruta: String) {
    val base = currentBackStack.value.firstOrNull { it.destination !is NavGraph }?.destination
        ?: graph.findStartDestination()
    if (base.route == ruta) {
        popBackStack(base.id, inclusive = false)
    } else {
        navigate(ruta) {
            popUpTo(base.id)
            launchSingleTop = true
        }
    }
}
