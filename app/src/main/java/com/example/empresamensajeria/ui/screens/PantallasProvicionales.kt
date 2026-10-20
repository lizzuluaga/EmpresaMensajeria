package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.listaServicios

@Composable
fun PantallaProvisional(titulo: String, onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = titulo, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onMenuClick) {
            Text("Abrir menú")
        }
    }
}


@Composable
fun CotizarScreen(servicioId: Int, onMenuClick: () -> Unit) {
    val servicio = listaServicios.find { it.id == servicioId }
    PantallaProvisional("Cotizar: ${servicio?.titulo ?: "servicio"}", onMenuClick)
}

@Composable
fun RastrearScreen(onMenuClick: () -> Unit) {
    PantallaProvisional("Rastrea tu pedido", onMenuClick)
}

@Composable
fun ContactoScreen(onMenuClick: () -> Unit) {
    PantallaProvisional("Contáctanos", onMenuClick)
}

@Composable
fun LoginScreen(onMenuClick: () -> Unit) {
    PantallaProvisional("Iniciar sesión", onMenuClick)
}