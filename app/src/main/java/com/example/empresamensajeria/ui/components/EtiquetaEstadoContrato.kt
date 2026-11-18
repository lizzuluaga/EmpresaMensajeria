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
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.data.local.entity.EstadoContrato
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun colorEstadoContrato(estado: EstadoContrato): Color = when (estado) {
    EstadoContrato.ACTIVO -> Color(0xFF43A047)
    EstadoContrato.FINALIZADO -> MaterialTheme.colorScheme.outline
    EstadoContrato.CANCELADO -> MaterialTheme.colorScheme.error
}

/** Chip de color con el estado del contrato (mismo estilo que EtiquetaEstado). */
@Composable
fun EtiquetaEstadoContrato(
    estado: EstadoContrato,
    modifier: Modifier = Modifier
) {
    val color = colorEstadoContrato(estado)
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

private const val DIA_MS = 24 * 60 * 60 * 1000L

private val formatoFecha = SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("es-CO"))

/** Fecha sin hora, por ejemplo "17 nov 2026". */
fun formatearFecha(milisegundos: Long): String = formatoFecha.format(Date(milisegundos))

/** Días que le quedan al contrato (fechaInicio + días - hoy), redondeando hacia arriba; 0 si ya pasó. */
fun diasRestantes(contrato: ContratoBodega, ahora: Long = System.currentTimeMillis()): Int {
    val restante = contrato.fechaInicio + contrato.dias * DIA_MS - ahora
    return if (restante <= 0) 0 else ((restante + DIA_MS - 1) / DIA_MS).toInt()
}

/** "12 días", "1 día" o "Vencido". Solo tiene sentido en contratos activos; en los demás devuelve "—". */
fun textoDiasRestantes(contrato: ContratoBodega): String {
    if (contrato.estado != EstadoContrato.ACTIVO) return "—"
    return when (val dias = diasRestantes(contrato)) {
        0 -> "Vencido"
        1 -> "1 día"
        else -> "$dias días"
    }
}

fun textoDias(dias: Int): String = if (dias == 1) "1 día" else "$dias días"
