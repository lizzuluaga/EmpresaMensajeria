package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.empresamensajeria.data.Envio
import com.example.empresamensajeria.data.buscarEnvio
import com.example.empresamensajeria.data.guiaValida
import com.example.empresamensajeria.data.listaEnvios
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.EtiquetaEstado
import com.example.empresamensajeria.ui.components.LineaTiempo
import com.example.empresamensajeria.ui.components.ProgresoEnvio
import com.example.empresamensajeria.ui.components.SeccionTitulo
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RastrearScreen(onMenuClick: () -> Unit) {
    var guia by remember { mutableStateOf("") }
    var envio by remember { mutableStateOf<Envio?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val estadoScroll = rememberScrollState()

    // Al aparecer el resultado, baja hasta él
    LaunchedEffect(envio) {
        if (envio != null) {
            delay(150)
            estadoScroll.animateScrollTo(estadoScroll.maxValue)
        }
    }

    fun buscar(texto: String) {
        val encontrado = buscarEnvio(texto)
        envio = encontrado
        error = if (encontrado == null) {
            "No encontramos un envío con la guía ${texto.trim()}"
        } else {
            null
        }
    }

    val formatoIncorrecto = guia.isNotEmpty() && !guiaValida(guia)

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Rastrea tu pedido", onMenuClick = onMenuClick)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(estadoScroll)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Consulta el estado de tu envío",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Escribe el número de guía que recibiste al enviar tu paquete.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = guia,
                onValueChange = { entrada ->
                    guia = entrada.filter { it.isLetterOrDigit() }.take(6).uppercase()
                    envio = null
                    error = null
                },
                label = { Text("Número de guía") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                isError = error != null || formatoIncorrecto,
                supportingText = {
                    Text(
                        error ?: if (formatoIncorrecto) {
                            "Formato: EY y cuatro números, por ejemplo EY1001"
                        } else {
                            "Formato: EY y cuatro números"
                        }
                    )
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Guías de ejemplo",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (ejemplo in listaEnvios.take(3)) {
                        AssistChip(
                            onClick = {
                                guia = ejemplo.guia
                                buscar(ejemplo.guia)
                            },
                            label = { Text(ejemplo.guia) }
                        )
                    }
                }
            }

            Button(
                onClick = { buscar(guia) },
                enabled = guiaValida(guia),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Buscar envío")
            }

            envio?.let { ResultadoRastreo(it) }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResultadoRastreo(envio: Envio) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Guía",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = envio.guia,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                EtiquetaEstado(estado = envio.estado)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = envio.servicio,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (envio.origen == envio.destino) {
                    "En ${envio.origen}"
                } else {
                    "${envio.origen} → ${envio.destino}"
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = envio.entrega,
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ProgresoEnvio(estadoActual = envio.estado)

            Spacer(modifier = Modifier.height(20.dp))

            SeccionTitulo("Historial")
            Spacer(modifier = Modifier.height(12.dp))
            LineaTiempo(eventos = envio.eventos)
        }
    }
}