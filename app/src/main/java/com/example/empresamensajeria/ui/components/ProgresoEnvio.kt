package com.example.empresamensajeria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.EstadoEnvio

@Composable
fun ProgresoEnvio(
    estadoActual: EstadoEnvio,
    modifier: Modifier = Modifier
) {
    val pasos = EstadoEnvio.entries
    val actual = estadoActual.ordinal
    val colorActivo = colorEstado(estadoActual)
    val colorInactivo = MaterialTheme.colorScheme.outlineVariant

    Row(modifier = modifier.fillMaxWidth()) {
        pasos.forEachIndexed { indice, paso ->
            val alcanzado = indice <= actual
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Línea hacia el paso anterior
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                when {
                                    indice == 0 -> Color.Transparent
                                    indice <= actual -> colorActivo
                                    else -> colorInactivo
                                }
                            )
                    )
                    // Círculo del paso
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (alcanzado) colorActivo else colorInactivo,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (alcanzado) {
                            Text(
                                text = "✓",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Línea hacia el paso siguiente
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                when {
                                    indice == pasos.lastIndex -> Color.Transparent
                                    indice < actual -> colorActivo
                                    else -> colorInactivo
                                }
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = paso.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (indice == actual) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}