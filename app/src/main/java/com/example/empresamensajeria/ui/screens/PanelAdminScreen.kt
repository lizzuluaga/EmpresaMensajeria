package com.example.empresamensajeria.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.empresamensajeria.data.aPesos
import com.example.empresamensajeria.ui.components.BarraSuperior
import com.example.empresamensajeria.ui.components.EtiquetaEstado
import com.example.empresamensajeria.ui.components.EtiquetaEstadoContrato
import com.example.empresamensajeria.ui.components.SeccionTitulo
import com.example.empresamensajeria.ui.components.colorEstado
import com.example.empresamensajeria.ui.components.colorEstadoContrato
import com.example.empresamensajeria.ui.viewmodel.PanelAdminViewModel
import com.example.empresamensajeria.ui.viewmodel.ResumenAdminState

private val pestanas = listOf("Resumen", "Envíos", "Clientes")

@Composable
fun PanelAdminScreen(
    onMenuClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerInventario: (Long) -> Unit = {},
    viewModel: PanelAdminViewModel = viewModel(factory = PanelAdminViewModel.Factory)
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Panel de administración",
                onMenuClick = onMenuClick,
                acciones = {
                    IconButton(onClick = {
                        viewModel.cerrarSesion()
                        onCerrarSesion()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(selectedTabIndex = pestana) {
                pestanas.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        text = { Text(titulo) }
                    )
                }
            }
            when (pestana) {
                0 -> PestanaResumen(viewModel)
                1 -> PestanaEnvios(viewModel)
                else -> PestanaClientes(onVerInventario = onVerInventario)
            }
        }
    }
}

// ---------- Pestaña Resumen ----------

@Composable
private fun PestanaResumen(viewModel: PanelAdminViewModel) {
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()

    if (resumen.cargando) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaCifra("📦", resumen.totalEnvios.toString(), "Envíos", Modifier.weight(1f))
            TarjetaCifra("💰", resumen.valorTotal.aPesos(), "Valor total", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaCifra(
                "👥",
                resumen.clientes.toString(),
                "Clientes (${resumen.clientesConCuenta} con cuenta)",
                Modifier.weight(1f)
            )
            TarjetaCifra("🏭", resumen.contratosActivos.toString(), "Contratos activos", Modifier.weight(1f))
        }

        SeccionTitulo("Envíos por estado")
        ConteosPorEstado(resumen)
    }
}

@Composable
private fun ConteosPorEstado(resumen: ResumenAdminState) {
    TarjetaSeccion {
        val total = resumen.totalEnvios
        resumen.enviosPorEstado.forEach { (estado, cantidad) ->
            FilaConteo(
                etiqueta = { EtiquetaEstado(estado) },
                cantidad = cantidad,
                total = total,
                color = colorEstado(estado)
            )
        }
    }

    SeccionTitulo("Contratos por estado")
    TarjetaSeccion {
        val total = resumen.contratosPorEstado.values.sum()
        resumen.contratosPorEstado.forEach { (estado, cantidad) ->
            FilaConteo(
                etiqueta = { EtiquetaEstadoContrato(estado) },
                cantidad = cantidad,
                total = total,
                color = colorEstadoContrato(estado)
            )
        }
    }
}

@Composable
private fun TarjetaCifra(emoji: String, valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
            Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun TarjetaSeccion(contenido: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            contenido()
        }
    }
}

/** Una fila: etiqueta del estado, barra proporcional al total y la cantidad. */
@Composable
private fun FilaConteo(
    etiqueta: @Composable () -> Unit,
    cantidad: Int,
    total: Int,
    color: Color
) {
    val proporcion = if (total == 0) 0f else cantidad.toFloat() / total
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(120.dp)) { etiqueta() }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(5.dp))
        ) {
            if (proporcion > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(proporcion)
                        .background(color, RoundedCornerShape(5.dp))
                )
            }
        }
        Text(
            text = cantidad.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )
    }
}
