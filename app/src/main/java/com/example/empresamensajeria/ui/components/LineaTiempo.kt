package com.example.empresamensajeria.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.EventoRastreo

@Composable
fun LineaTiempo(
    eventos: List<EventoRastreo>,
    modifier: Modifier = Modifier
) {
    val colorPrincipal = MaterialTheme.colorScheme.primary
    val colorLinea = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        eventos.forEachIndexed { indice, evento ->
            val esReciente = indice == 0
            val esUltimo = indice == eventos.lastIndex

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(if (esReciente) 14.dp else 10.dp)
                            .background(
                                if (esReciente) colorPrincipal else colorLinea,
                                CircleShape
                            )
                    )
                    if (!esUltimo) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(colorLinea)
                        )
                    }
                }
                Column(
                    modifier = Modifier.padding(
                        start = 8.dp,
                        bottom = if (esUltimo) 0.dp else 16.dp
                    )
                ) {
                    Text(
                        text = evento.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (esReciente) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = evento.lugar,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = evento.fecha,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}