package com.example.empresamensajeria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.local.entity.EstadoEnvio

@Composable
fun colorEstado(estado: EstadoEnvio): Color = when (estado) {
    // Gris: todavía no se ha recibido el paquete
    EstadoEnvio.PENDIENTE -> MaterialTheme.colorScheme.outline
    EstadoEnvio.RECIBIDO -> MaterialTheme.colorScheme.secondary
    EstadoEnvio.EN_TRANSITO -> MaterialTheme.colorScheme.primary
    EstadoEnvio.EN_REPARTO -> Color(0xFF00897B)
    EstadoEnvio.ENTREGADO -> Color(0xFF43A047)
}

@Composable
fun EtiquetaEstado(
    estado: EstadoEnvio,
    modifier: Modifier = Modifier
) {
    val color = colorEstado(estado)
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = estado.etiqueta,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}