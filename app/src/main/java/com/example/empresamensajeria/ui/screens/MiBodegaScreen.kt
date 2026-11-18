package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.empresamensajeria.data.aPesos
import com.example.empresamensajeria.data.local.entity.ContratoBodega
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.EtiquetaEstadoContrato
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.components.formatearFecha
import com.example.empresamensajeria.ui.components.textoDias
import com.example.empresamensajeria.ui.components.textoDiasRestantes
import com.example.empresamensajeria.ui.viewmodel.MiBodegaViewModel

@Composable
fun MiBodegaScreen(
    onMenuClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    onContratarOtro: () -> Unit,
    onAbrirContrato: (Long) -> Unit,
    viewModel: MiBodegaViewModel = viewModel(factory = MiBodegaViewModel.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Mi bodega", onMenuClick = onMenuClick) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Saludo(estado.nombre, onCerrarSesion, onContratarOtro) }

            item { SeccionTitulo("Mis contratos") }

            when {
                estado.cargando -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                estado.contratos.isEmpty() -> item {
                    Text(
                        text = "Todavía no tienes contratos de bodega. Toca \"Contratar otro espacio\" para cotizar uno.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }

                else -> items(estado.contratos, key = { it.id }) { contrato ->
                    TarjetaContrato(contrato, onClick = { onAbrirContrato(contrato.id) })
                }
            }
        }
    }
}

@Composable
private fun Saludo(
    nombre: String,
    onCerrarSesion: () -> Unit,
    onContratarOtro: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📦", style = MaterialTheme.typography.headlineMedium)
        }
        Text(
            text = if (nombre.isBlank()) "Hola" else "Hola, $nombre",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Toca un contrato para ver y administrar su inventario.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onContratarOtro,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("Contratar otro espacio", textAlign = TextAlign.Center)
            }
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun TarjetaContrato(contrato: ContratoBodega, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Contrato #${contrato.id}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Espacio ${contrato.espacio.lowercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                EtiquetaEstadoContrato(contrato.estado)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            DatoContrato("Días contratados", textoDias(contrato.dias))
            DatoContrato("Días restantes", textoDiasRestantes(contrato))
            DatoContrato("Fecha de inicio", formatearFecha(contrato.fechaInicio))
            DatoContrato("Valor", contrato.valor.aPesos())

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ver inventario →",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun DatoContrato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
